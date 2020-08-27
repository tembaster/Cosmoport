package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/rest/ships")
public class ShipRestController {

    private ShipService shipService;

    @Autowired
    public ShipRestController(ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping
    public List<Ship> getShips(@RequestParam(required = false) String name,
                               @RequestParam(required = false) String planet,
                               @RequestParam(required = false) ShipType shipType,
                               @RequestParam(required = false) Long after,
                               @RequestParam(required = false) Long before,
                               @RequestParam(required = false) Boolean isUsed,
                               @RequestParam(required = false) Double minSpeed,
                               @RequestParam(required = false) Double maxSpeed,
                               @RequestParam(required = false) Integer minCrewSize,
                               @RequestParam(required = false) Integer maxCrewSize,
                               @RequestParam(required = false) Double minRating,
                               @RequestParam(required = false) Double maxRating,
                               @RequestParam(required = false) ShipOrder order,
                               @RequestParam(required = false) Integer pageNumber,
                               @RequestParam(required = false) Integer pageSize) {
        List<Ship> filteredList = shipService.getShipsList(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize,
                maxCrewSize, minRating, maxRating);
        return shipService.getSortedList(filteredList, order, pageNumber, pageSize);
    }

    @GetMapping("/count")
    public Integer getShipsCount(@RequestParam(required = false) String name,
                                 @RequestParam(required = false) String planet,
                                 @RequestParam(required = false) ShipType shipType,
                                 @RequestParam(required = false) Long after,
                                 @RequestParam(required = false) Long before,
                                 @RequestParam(required = false) Boolean isUsed,
                                 @RequestParam(required = false) Double minSpeed,
                                 @RequestParam(required = false) Double maxSpeed,
                                 @RequestParam(required = false) Integer minCrewSize,
                                 @RequestParam(required = false) Integer maxCrewSize,
                                 @RequestParam(required = false) Double minRating,
                                 @RequestParam(required = false) Double maxRating) {
        return shipService.getShipsCount(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize,
                maxCrewSize, minRating, maxRating);
    }

    @PostMapping
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        return shipService.createShip(ship);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ship> getShip(@PathVariable Long id) {
        if (!isValid(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return shipService.getShipById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Ship> deleteShip(@PathVariable Long id) {
        if (!isValid(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return shipService.deleteShipById(id);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Ship> updateShip(@RequestBody Ship ship,
                                           @PathVariable("id") Long id) {
        if (!isValid(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return shipService.updateShip(ship, id);
    }

    private Boolean isValid(Long id) {
        return id > 0;
    }
}
