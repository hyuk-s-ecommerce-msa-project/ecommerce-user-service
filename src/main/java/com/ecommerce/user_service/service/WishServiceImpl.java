package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.WishDto;
import com.ecommerce.user_service.entity.WishCategory;
import com.ecommerce.user_service.entity.WishEntity;
import com.ecommerce.user_service.entity.WishGenre;
import com.ecommerce.user_service.exception.WishNotFoundException;
import com.ecommerce.user_service.repository.WishRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishServiceImpl implements WishService {
    private final WishRepository wishRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<WishDto> getWishList(String userId) {
        List<WishEntity> wish = wishRepository.findByUserId(userId);

        if (wish.isEmpty()) {
            return new ArrayList<>();
        }

        return convertEntityToDto(wish);
    }

    @Override
    @Transactional
    public WishDto addToWish(WishDto wishDto, String userId) {
        WishEntity existingWish = wishRepository.findByUserIdAndProductId(userId, wishDto.getProductId());

        if (existingWish != null) {
            throw new RuntimeException("Wish already exists");
        }

        String wishId = "WISH-" + UUID.randomUUID().toString();

        WishEntity wishList = WishEntity.create(
                wishId, userId, wishDto.getProductId(), wishDto.getProductName(),
                wishDto.getPrice() , wishDto.getThumbnailUrl()
        );

        if (wishDto.getCategories() != null) {
            wishDto.getCategories().stream()
                    .map(c -> WishCategory.create(c, wishList))
                    .forEach(category -> wishList.getCategories().add(category));
        }

        if (wishDto.getGenres() != null) {
            wishDto.getGenres().stream()
                    .map(m -> WishGenre.create(m, wishList))
                    .forEach(m -> wishList.getGenres().add(m));
        }

        wishRepository.save(wishList);

        WishDto result = modelMapper.map(wishList, WishDto.class);
        result.setCategories(wishDto.getCategories());
        result.setGenres(wishDto.getGenres());

        return result;
    }

    @Override
    @Transactional
    public void removeWish(String wishId, String userId) {
        WishEntity wishEntity = wishRepository.findByWishId(wishId);

        if (wishEntity == null) {
            throw new WishNotFoundException("Wish not found");
        }

        if (!wishEntity.getUserId().equals(userId)) {
            throw new RuntimeException("You do not have permission to remove this wish.");
        }

        wishRepository.deleteById(wishEntity.getId());
    }

    private List<WishDto> convertEntityToDto(List<WishEntity> wishEntityList) {
        return wishEntityList.stream()
                .map(entity -> {
                    WishDto wishDto = modelMapper.map(entity, WishDto.class);

                    wishDto.setCategories(entity.getCategories().stream().map(WishCategory::getCategoryName).toList());
                    wishDto.setGenres(entity.getGenres().stream().map(WishGenre::getGenreName).toList());

                    return wishDto;
                }).toList();
    }
}
