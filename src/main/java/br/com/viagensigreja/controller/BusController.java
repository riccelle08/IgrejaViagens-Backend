package br.com.viagensigreja.controller;

import br.com.viagensigreja.model.Bus;
import br.com.viagensigreja.service.BusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buses")
@CrossOrigin
public class BusController {

    private final BusService service;

    public BusController(BusService service) {
        this.service = service;
    }

    @PostMapping
    public Bus criar(@RequestBody Bus bus) {
        return service.salvar(bus);
    }

    @GetMapping("/trip/{tripId}")
    public List<Bus> listarPorViagem(@PathVariable String tripId) {
        return service.buscarPorTrip(tripId);
    }
}
