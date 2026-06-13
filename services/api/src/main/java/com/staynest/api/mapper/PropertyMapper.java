package com.staynest.api.mapper;

import com.staynest.api.dto.response.*;
import com.staynest.api.entity.Amenity;
import com.staynest.api.entity.Property;
import com.staynest.api.entity.PropertyAvailability;
import com.staynest.api.entity.PropertyPhoto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    @Mapping(target = "host", source = "host")
    @Mapping(target = "photos", source = "photos")
    @Mapping(target = "amenities", source = "amenities")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    PropertyResponse toPropertyResponse(Property property);

    @Mapping(target = "coverPhotoUrl", expression = "java(extractCoverPhotoUrl(property))")
    @Mapping(target = "hostFirstName", source = "host.firstName")
    @Mapping(target = "hostLastName", source = "host.lastName")
    PropertySummaryResponse toPropertySummaryResponse(Property property);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "profilePictureUrl", source = "profilePictureUrl")
    @Mapping(target = "createdAt", source = "createdAt")
    PropertyResponse.HostSummary toHostSummary(com.staynest.api.entity.User host);

    AmenityResponse toAmenityResponse(Amenity amenity);

    @Mapping(target = "isCover", source = "cover")
    PropertyPhotoResponse toPropertyPhotoResponse(PropertyPhoto photo);

    PropertyAvailabilityResponse toPropertyAvailabilityResponse(PropertyAvailability availability);

    default String extractCoverPhotoUrl(Property property) {
        if (property.getPhotos() == null || property.getPhotos().isEmpty()) {
            return null;
        }
        return property.getPhotos().stream()
                .filter(PropertyPhoto::isCover)
                .findFirst()
                .map(PropertyPhoto::getUrl)
                .orElse(property.getPhotos().get(0).getUrl());
    }
}
