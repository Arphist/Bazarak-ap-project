package com.bazarak.service;

import com.bazarak.entity.City;
import com.bazarak.exception.city.CityHasAdvertisementsException;
import com.bazarak.exception.city.CityNameAlreadyExistsException;
import com.bazarak.exception.city.CityNotFoundException;
import com.bazarak.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CityService {


}