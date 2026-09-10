package br.com.viagensigreja.service;

import br.com.viagensigreja.model.Payment;
import br.com.viagensigreja.repository.PaymentRepository;
import br.com.viagensigreja.security.ResourceAuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final ResourceAuthorizationService authorization;
    private final ObjectMapper objectMapper;

    public PaymentService(
            PaymentRepository repository,
            ResourceAuthorizationService authorization,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.authorization = authorization;
        this.objectMapper = objectMapper;
    }

    public Payment salvar(Payment payment) {
        if (payment.getId() == null || payment.getId().isBlank()) {
            payment.setId(payment.getUserCpf() + "_" + payment.getTripId());
        }
        return repository.save(payment);
    }

    public List<Payment> buscarPorUsuario(String cpf) {
        return repository.findByUserCpf(cpf);
    }

    public List<Payment> listarVisiveis(Authentication authentication) {
        if (authorization.isAdmin(authentication)) {
            return repository.findAll();
        }

        String cpf = authorization.authenticatedCpf(authentication);
        Set<String> tripIds = authorization.accessibleTripIds(authentication);
        return repository.findAll().stream()
                .filter(payment -> authorization.belongsTo(payment.getUserCpf(), cpf))
                .filter(payment -> tripIds.contains(payment.getTripId()))
                .toList();
    }

    public List<Payment> buscarPorUsuarioVisivel(String cpf, Authentication authentication) {
        if (authorization.isAdmin(authentication)) {
            return repository.findByUserCpf(cpf);
        }
        return listarVisiveis(authentication);
    }

    /**
     * MantÃ©m o contrato bulk usado pelo React atual. Para ADMIN a semÃ¢ntica
     * legada de substituiÃ§Ã£o integral Ã© preservada. Para TRAVELER o mesmo
     * endpoint funciona apenas como upsert dos prÃ³prios registros, sem excluir
     * pagamentos omitidos e sem permitir alteraÃ§Ã£o de campos administrativos.
     */
    @Transactional
    public List<Payment> substituirCompativel(
            List<Payment> payments,
            Authentication authentication
    ) {
        if (authorization.isAdmin(authentication)) {
            return substituirTodosComoAdmin(payments);
        }
        if (!authorization.isTraveler(authentication)) {
            throw new AccessDeniedException("Perfil sem permissÃ£o para editar pagamentos.");
        }
        return atualizarPagamentosDoViajante(payments, authentication);
    }

    private List<Payment> substituirTodosComoAdmin(List<Payment> payments) {
        repository.deleteAll();
        payments.forEach(payment -> {
            if (payment.getId() == null || payment.getId().isBlank()) {
                payment.setId(payment.getUserCpf() + "_" + payment.getTripId());
            }
        });
        return repository.saveAll(payments);
    }

    private List<Payment> atualizarPagamentosDoViajante(
            List<Payment> submitted,
            Authentication authentication
    ) {
        String cpf = authorization.authenticatedCpf(authentication);
        Set<String> accessibleTripIds = authorization.accessibleTripIds(authentication);
        List<Payment> current = repository.findAll();
        List<Payment> prepared = new ArrayList<>();
        Set<String> submittedTrips = new java.util.HashSet<>();

        for (Payment candidate : submitted) {
            validateTravelerIdentity(candidate, cpf, accessibleTripIds);
            if (!submittedTrips.add(candidate.getTripId())) {
                throw badRequest("Foi enviado mais de um pagamento para a mesma viagem.");
            }

            List<Payment> matching = current.stream()
                    .filter(payment -> authorization.belongsTo(payment.getUserCpf(), cpf))
                    .filter(payment -> Objects.equals(payment.getTripId(), candidate.getTripId()))
                    .toList();
            if (matching.size() > 1) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "HÃ¡ pagamentos duplicados que precisam ser corrigidos por um administrador."
                );
            }

            prepared.add(mergeTravelerPayment(
                    matching.isEmpty() ? null : matching.get(0),
                    candidate,
                    cpf
            ));
        }

        repository.saveAll(prepared);
        return repository.findAll().stream()
                .filter(payment -> authorization.belongsTo(payment.getUserCpf(), cpf))
                .filter(payment -> accessibleTripIds.contains(payment.getTripId()))
                .toList();
    }

    private void validateTravelerIdentity(
            Payment candidate,
            String cpf,
            Set<String> accessibleTripIds
    ) {
        if (candidate == null
                || !authorization.belongsTo(candidate.getUserCpf(), cpf)
                || candidate.getTripId() == null
                || !accessibleTripIds.contains(candidate.getTripId())) {
            throw new AccessDeniedException(
                    "Viajantes sÃ³ podem editar o prÃ³prio pagamento em viagens associadas."
            );
        }
    }

    private Payment mergeTravelerPayment(Payment existing, Payment candidate, String cpf) {
        int totalInstallments;
        int dueDay;
        boolean locked;
        int paidInstallments;
        String id;

        if (existing == null) {
            validatePlan(candidate.getTotalInstallments(), candidate.getDueDay(), 0);
            totalInstallments = candidate.getTotalInstallments();
            dueDay = candidate.getDueDay();
            locked = candidate.isLocked();
            paidInstallments = 0;
            id = cpf + "_" + candidate.getTripId();
        } else {
            id = existing.getId();
            paidInstallments = existing.getPaidInstallments();
            if (existing.isLocked()) {
                totalInstallments = existing.getTotalInstallments();
                dueDay = existing.getDueDay();
                locked = true;
            } else {
                validatePlan(
                        candidate.getTotalInstallments(),
                        candidate.getDueDay(),
                        paidInstallments
                );
                totalInstallments = candidate.getTotalInstallments();
                dueDay = candidate.getDueDay();
                locked = candidate.isLocked();
            }
        }

        String receiptsJson = mergeTravelerReceipts(
                existing == null ? null : existing.getReceiptsJson(),
                candidate.getReceiptsJson(),
                totalInstallments,
                locked
        );

        return new Payment(
                id,
                cpf,
                candidate.getTripId(),
                totalInstallments,
                paidInstallments,
                dueDay,
                locked,
                receiptsJson
        );
    }

    private void validatePlan(int totalInstallments, int dueDay, int paidInstallments) {
        if (totalInstallments < 1 || totalInstallments > 24) {
            throw badRequest("A quantidade de parcelas deve estar entre 1 e 24.");
        }
        if (dueDay < 1 || dueDay > 31) {
            throw badRequest("O dia de vencimento deve estar entre 1 e 31.");
        }
        if (totalInstallments < paidInstallments) {
            throw badRequest("O total de parcelas nÃ£o pode ser menor que as parcelas jÃ¡ pagas.");
        }
    }

    private String mergeTravelerReceipts(
            String storedJson,
            String submittedJson,
            int totalInstallments,
            boolean locked
    ) {
        Map<String, Object> stored = parseJsonObject(storedJson, true);
        Map<String, Object> submitted = parseJsonObject(submittedJson, false);
        Map<String, Object> merged = new LinkedHashMap<>(stored);

        for (Map.Entry<String, Object> entry : submitted.entrySet()) {
            Map<String, Object> nextReceipt = asObject(entry.getValue());
            if (nextReceipt == null) {
                throw badRequest("O comprovante enviado possui formato invÃ¡lido.");
            }

            Map<String, Object> currentReceipt = asObject(stored.get(entry.getKey()));
            if (currentReceipt == null) {
                if (stored.containsKey(entry.getKey())) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Um comprovante persistido exige revisÃ£o administrativa."
                    );
                }
                validateReceiptUpload(entry.getKey(), nextReceipt, totalInstallments, locked);
                merged.put(entry.getKey(), sanitizeNewReceipt(nextReceipt));
                continue;
            }

            if (!receiptFileChanged(currentReceipt, nextReceipt)
                    || "approved".equals(currentReceipt.get("status"))) {
                merged.put(entry.getKey(), currentReceipt);
                continue;
            }

            validateReceiptUpload(entry.getKey(), nextReceipt, totalInstallments, locked);
            Map<String, Object> reuploaded = new LinkedHashMap<>(currentReceipt);
            copyReceiptFileFields(nextReceipt, reuploaded);
            reuploaded.put("status", "pending");
            reuploaded.put("note", "");
            merged.put(entry.getKey(), reuploaded);
        }

        try {
            return objectMapper.writeValueAsString(merged);
        } catch (Exception exception) {
            throw new IllegalStateException("NÃ£o foi possÃ­vel serializar os comprovantes.", exception);
        }
    }

    private void validateReceiptUpload(
            String installment,
            Map<String, Object> receipt,
            int totalInstallments,
            boolean locked
    ) {
        int installmentNumber;
        try {
            installmentNumber = Integer.parseInt(installment);
        } catch (NumberFormatException exception) {
            throw badRequest("O nÃºmero da parcela do comprovante Ã© invÃ¡lido.");
        }
        if (!locked || installmentNumber < 1 || installmentNumber > totalInstallments) {
            throw badRequest("O comprovante nÃ£o pertence a uma parcela vÃ¡lida do plano.");
        }
        if (!(receipt.get("data") instanceof String data) || data.isBlank()) {
            throw badRequest("O conteÃºdo do comprovante Ã© obrigatÃ³rio.");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonObject(String json, boolean storedValue) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            Object parsed = objectMapper.readValue(json, Map.class);
            if (parsed instanceof Map<?, ?> map) {
                Map<String, Object> result = new LinkedHashMap<>();
                map.forEach((key, value) -> result.put(String.valueOf(key), value));
                return result;
            }
        } catch (Exception ignored) {
            // A mensagem abaixo distingue dado persistido inconsistente de entrada invÃ¡lida.
        }

        if (storedValue) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Os comprovantes persistidos estÃ£o invÃ¡lidos e exigem revisÃ£o administrativa."
            );
        }
        throw badRequest("O JSON de comprovantes enviado Ã© invÃ¡lido.");
    }

    private Map<String, Object> asObject(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, entryValue) -> result.put(String.valueOf(key), entryValue));
        return result;
    }

    private Map<String, Object> sanitizeNewReceipt(Map<String, Object> submitted) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        copyReceiptFileFields(submitted, sanitized);
        sanitized.put("status", "pending");
        sanitized.put("note", "");
        return sanitized;
    }

    private void copyReceiptFileFields(Map<String, Object> source, Map<String, Object> target) {
        for (String field : List.of("data", "date", "filename", "fileName", "type")) {
            Object value = source.get(field);
            if (value instanceof String) {
                target.put(field, value);
            }
        }
    }

    private boolean receiptFileChanged(
            Map<String, Object> current,
            Map<String, Object> submitted
    ) {
        return List.of("data", "date", "filename", "fileName", "type").stream()
                .anyMatch(field -> !Objects.equals(current.get(field), submitted.get(field)));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
