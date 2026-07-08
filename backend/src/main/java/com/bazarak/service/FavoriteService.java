package com.bazarak.service;

import com.bazarak.entity.Advertisement;
import com.bazarak.entity.Favorite;
import com.bazarak.entity.User;
import com.bazarak.exception.favorite.*;
import com.bazarak.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private AdvertisementService advertisementService;

    /**
     * Add an ad to user's favorites
     */
    @Transactional
    public Favorite addToFavorite(User user, Advertisement ad) {
        if (!favoriteRepository.existsByUserAndAdvertisement(user, ad)) {
            throw new InvalidOperationException("This ad is already in your favorites");
        }
        // Create and save favorite
        Favorite favorite = new Favorite(user, ad);
        return favoriteRepository.save(favorite);
    }

    /**
     * Remove an ad from user's favorites
     */
    @Transactional
    public void removeFromFavorite(User user,Advertisement ad){
        if (!favoriteRepository.existsByUserAndAdvertisement(user,ad)){
            throw new InvalidOperationException("This ad isn't available in your favorites");
        }

        favoriteRepository.deleteByUserAndAd(user,ad);
    }

    /**
     * Get all favorites for a user with ad details
     */
    public List<Favorite> getUserFavorites(User user){
        return favoriteRepository.findByUserWithAdDetails(user);
    }

    /**
     * Get all favorite ads for a user (as Ad objects)
     */
    public List<Advertisement> getUserFavoriteAds(Long userId) {
        User user = userService.getUserById(userId);
        return favoriteRepository.findByUserWithAdDetails(user).stream()
                .map(Favorite::getAdvertisement)
                .toList();
    }
}
