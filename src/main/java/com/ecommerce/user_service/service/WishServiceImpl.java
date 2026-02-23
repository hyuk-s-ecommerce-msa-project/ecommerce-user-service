package com.ecommerce.user_service.service;

import com.ecommerce.user_service.client.CatalogServiceClient;
import com.ecommerce.user_service.dto.WishDto;
import com.ecommerce.user_service.entity.WishCategory;
import com.ecommerce.user_service.entity.WishEntity;
import com.ecommerce.user_service.entity.WishGenre;
import com.ecommerce.user_service.exception.CatalogServiceException;
import com.ecommerce.user_service.exception.WishNotFoundException;
import com.ecommerce.user_service.repository.WishRepository;
import com.ecommerce.user_service.vo.ResponseWish;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishServiceImpl implements WishService {
    private final WishRepository wishRepository;
    private final ModelMapper modelMapper;
    private final CatalogServiceClient catalogServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

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
    public WishDto addToWish(String userId, String productId) {
        WishEntity existingWish = wishRepository.findByUserIdAndProductId(userId, productId);

        if (existingWish != null) {
            throw new RuntimeException("Wish already exists");
        }

        String wishId = "WISH-" + UUID.randomUUID().toString();

//        ResponseWish wishProductList = catalogServiceClient.getCatalogs(productId);
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("catalog-circuitBreaker");
        ResponseWish wishProductList = circuitBreaker.run(() -> catalogServiceClient.getCatalogs(productId),
                throwable -> {
                    throw new CatalogServiceException("찜하기 기능은 현재 오류가 있습니다. 나중에 다시 시도해주세요.");
                });

        WishEntity wishList = WishEntity.create(
                wishId, userId, wishProductList.getProductId(), wishProductList.getProductName(),
                wishProductList.getPrice() , wishProductList.getHeaderImage()
        );

        if (wishProductList.getCategories() != null) {
            wishProductList.getCategories().stream()
                    .map(c -> WishCategory.create(c, wishList))
                    .forEach(category -> wishList.getCategories().add(category));
        }

        if (wishProductList.getGenres() != null) {
            wishProductList.getGenres().stream()
                    .map(g -> WishGenre.create(g, wishList))
                    .forEach(genre -> wishList.getGenres().add(genre));
        }

        wishRepository.save(wishList);

        WishDto result = modelMapper.map(wishList, WishDto.class);
        result.setCategories(wishProductList.getCategories());
        result.setGenres(wishProductList.getGenres());

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
