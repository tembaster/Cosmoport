package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShipService {

    private ShipRepository shipRepository;

    @Autowired
    public ShipService(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    public List<Ship> getShipsList(String name,
                                   String planet,
                                   ShipType shipType,
                                   Long after,
                                   Long before,
                                   Boolean isUsed,
                                   Double minSpeed,
                                   Double maxSpeed,
                                   Integer minCrewSize,
                                   Integer maxCrewSize,
                                   Double minRating,
                                   Double maxRating) {
        List<Ship> result = shipRepository.findAll();
        if (name != null) {
            result = result.stream()
                    .filter(ship -> ship.getName().contains(name))
                    .collect(Collectors.toList());
        }
        if (planet != null) {
            result = result.stream()
                    .filter(ship -> ship.getPlanet().contains(planet))
                    .collect(Collectors.toList());
        }
        if (shipType != null) {
            result = result.stream()
                    .filter(ship -> ship.getShipType().equals(shipType))
                    .collect(Collectors.toList());
        }
        if (after != null) {
            result = result.stream()
                    .filter(ship -> ship.getProdDate().after(new Date(after)))
                    .collect(Collectors.toList());
        }
        if (before != null) {
            result = result.stream()
                    .filter(ship -> ship.getProdDate().before(new Date(before)))
                    .collect(Collectors.toList());
        }
        if (isUsed != null) {
            result = result.stream()
                    .filter(ship -> ship.getUsed().equals(isUsed))
                    .collect(Collectors.toList());
        }
        if (minSpeed != null) {
            result = result.stream()
                    .filter(ship -> ship.getSpeed() >= minSpeed)
                    .collect(Collectors.toList());
        }
        if (maxSpeed != null) {
            result = result.stream()
                    .filter(ship -> ship.getSpeed() <= maxSpeed)
                    .collect(Collectors.toList());
        }
        if (minCrewSize != null) {
            result = result.stream()
                    .filter(ship -> ship.getCrewSize() >= minCrewSize)
                    .collect(Collectors.toList());
        }
        if (maxCrewSize != null) {
            result = result.stream()
                    .filter(ship -> ship.getCrewSize() <= maxCrewSize)
                    .collect(Collectors.toList());
        }
        if (minRating != null) {
            result = result.stream()
                    .filter(ship -> ship.getRating() >= minRating)
                    .collect(Collectors.toList());
        }
        if (maxRating != null) {
            result = result.stream()
                    .filter(ship -> ship.getRating() <= maxRating)
                    .collect(Collectors.toList());
        }
        return result;
    }

    public List<Ship> getSortedList(List<Ship> list, ShipOrder order, Integer pageNumber, Integer pageSize) {
        if (pageNumber == null) {
            pageNumber = 0;
        }
        if (pageSize == null) {
            pageSize = 3;
        }
        return list.stream()
                .sorted(getComparator(order))
                .skip(pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public int getShipsCount(String name,
                             String planet,
                             ShipType shipType,
                             Long after,
                             Long before,
                             Boolean isUsed,
                             Double minSpeed,
                             Double maxSpeed,
                             Integer minCrewSize,
                             Integer maxCrewSize,
                             Double minRating,
                             Double maxRating) {
        return getShipsList(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize,
                maxCrewSize, minRating, maxRating).size();
    }

    private Comparator<Ship> getComparator(ShipOrder order) {
        Comparator<Ship> comparator = null;
        if (order == null || order.getFieldName().equals("id")) {
            comparator = Comparator.comparing(Ship::getId);
        } else if (order.getFieldName().equals("speed")) {
            comparator = Comparator.comparing(Ship::getSpeed);
        } else if (order.getFieldName().equals("prodDate")) {
            comparator = Comparator.comparing(Ship::getProdDate);
        } else if (order.getFieldName().equals("rating")) {
            comparator = Comparator.comparing(Ship::getRating);
        }
        return comparator;
    }

    public ResponseEntity<Ship> createShip(Ship ship) {
        if (ship == null ||
                ship.getName() == null ||
                ship.getName().isEmpty() ||
                ship.getName().length() > 50 ||
                ship.getPlanet() == null ||
                ship.getPlanet().isEmpty() ||
                ship.getPlanet().length() > 50 ||
                ship.getShipType() == null ||
                ship.getProdDate() == null ||
                ShipService.getProdDateYear(ship.getProdDate()) < 2800 ||
                ShipService.getProdDateYear(ship.getProdDate()) > 3019 ||
                ship.getSpeed() == null ||
                ship.getSpeed() < 0.01d ||
                ship.getSpeed() > 0.99d ||
                ship.getCrewSize() == null ||
                ship.getCrewSize() < 1 ||
                ship.getCrewSize() > 9999) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }
        ship.setRating(computeRating(ship));
        ship.setSpeed((double) Math.round(ship.getSpeed() * 100) / 100);
        Ship shipToSave = shipRepository.save(ship);
        return new ResponseEntity<>(shipToSave, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Ship> getShipById(Long id) {
        if (!shipRepository.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Ship result = shipRepository.getOne(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }

    @Transactional
    public ResponseEntity<Ship> deleteShipById(Long id) {
        if (!shipRepository.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            shipRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @Transactional
    public ResponseEntity<Ship> updateShip(Ship ship, Long id) {
        if (ship != null &&
            ship.getName() == null &&
            ship.getPlanet() == null &&
            ship.getUsed() == null &&
            ship.getShipType() == null &&
            ship.getProdDate() == null &&
            ship.getSpeed() == null &&
            ship.getCrewSize() == null &&
            shipRepository.findById(id).isPresent()) {
                return new ResponseEntity<>(shipRepository.getOne(id), HttpStatus.OK);
        }

        if (!shipRepository.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Ship shipToUpdate = getShipById(id).getBody();

        if (ship == null || shipToUpdate == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (ship.getName() != null) {
            if (ship.getName().length() > 50 || ship.getName().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            shipToUpdate.setName(ship.getName());
        }
        if (ship.getPlanet() != null) {
            if (ship.getPlanet().length() > 50 || ship.getPlanet().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            shipToUpdate.setPlanet(ship.getPlanet());
        }
        if (ship.getShipType() != null) {
            shipToUpdate.setShipType(ship.getShipType());
        }
        if (ship.getProdDate() != null) {
            if (getProdDateYear(ship.getProdDate()) < 2800 || getProdDateYear(ship.getProdDate()) > 3019) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            shipToUpdate.setProdDate(ship.getProdDate());
        }
        if (ship.getUsed() != null) {
            shipToUpdate.setUsed(ship.getUsed());
        } else {
            shipToUpdate.setUsed(false);
        }
        if (ship.getSpeed() != null) {
            if (ship.getSpeed() < 0.01d || ship.getSpeed() > 0.99d) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            shipToUpdate.setSpeed(ship.getSpeed());
        }
        if (ship.getCrewSize() != null) {
            if (ship.getCrewSize() < 1 || ship.getCrewSize() > 9999) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            shipToUpdate.setCrewSize(ship.getCrewSize());
        }
        shipToUpdate.setRating(computeRating(shipToUpdate));
        shipRepository.save(shipToUpdate);
        return new ResponseEntity<>(shipToUpdate, HttpStatus.OK);
    }


    public static Integer getProdDateYear(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear();
    }

    private Double computeRating(Ship ship) {
        double speed = ship.getSpeed();
        double usedRatio = ship.getUsed() ? 0.5d : 1.0d;
        int currentYear = 3019;
        int productionDate = getProdDateYear(ship.getProdDate());
        double result = (80 * speed * usedRatio) / (double) (currentYear - productionDate + 1);
        return (double) Math.round(result * 100) / 100;
    }
}
