package com.vaadin.starter.bakery.backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.vaadin.starter.bakery.backend.data.entity.PickupLocation;
import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.repositories.PickupLocationRepository;

/**
 * Service class for managing {@link PickupLocation} entities.
 * <p>
 * Provides CRUD operations and filtering functionality.
 * </p>
 */
@Service
public class PickupLocationService implements FilterableCrudService<PickupLocation> {

    private final PickupLocationRepository pickupLocationRepository;

    /**
     * Constructs a new {@code PickupLocationService} with the specified repository.
     *
     * @param pickupLocationRepository the repository used to access pickup locations
     */
    @Autowired
    public PickupLocationService(PickupLocationRepository pickupLocationRepository) {
        this.pickupLocationRepository = pickupLocationRepository;
    }

    /**
     * Finds {@link PickupLocation} entities that match the given filter string.
     * <p>
     * If the filter is present, it searches by name ignoring case and using wildcard matching.
     * If the filter is empty, it returns all pickup locations with pagination.
     * </p>
     *
     * @param filter   an optional string to filter by pickup location name
     * @param pageable pagination information
     * @return a page of matching pickup locations
     */
    public Page<PickupLocation> findAnyMatching(Optional<String> filter, Pageable pageable) {
        if (filter.isPresent()) {
            String repositoryFilter = "%" + filter.get() + "%";
            return pickupLocationRepository.findByNameLikeIgnoreCase(repositoryFilter, pageable);
        } else {
            return pickupLocationRepository.findAll(pageable);
        }
    }

    /**
     * Counts the number of {@link PickupLocation} entities that match the given filter string.
     * <p>
     * If the filter is present, it counts by name ignoring case and using wildcard matching.
     * If the filter is empty, it returns the total number of pickup locations.
     * </p>
     *
     * @param filter an optional string to filter by pickup location name
     * @return the number of matching pickup locations
     */
    public long countAnyMatching(Optional<String> filter) {
        if (filter.isPresent()) {
            String repositoryFilter = "%" + filter.get() + "%";
            return pickupLocationRepository.countByNameLikeIgnoreCase(repositoryFilter);
        } else {
            return pickupLocationRepository.count();
        }
    }

    /**
     * Returns the default {@link PickupLocation}.
     * <p>
     * This method retrieves the first pickup location from the database.
     * </p>
     *
     * @return the default pickup location
     */
    public PickupLocation getDefault() {
        return findAnyMatching(Optional.empty(), PageRequest.of(0, 1)).iterator().next();
    }

    /**
     * Returns the repository used by this service.
     *
     * @return the {@link JpaRepository} for {@link PickupLocation}
     */
    @Override
    public JpaRepository<PickupLocation, Long> getRepository() {
        return pickupLocationRepository;
    }

    /**
     * Creates a new instance of {@link PickupLocation}.
     *
     * @param currentUser the current user creating the new entity
     * @return a new {@link PickupLocation} instance
     */
    @Override
    public PickupLocation createNew(User currentUser) {
        return new PickupLocation();
    }
}
