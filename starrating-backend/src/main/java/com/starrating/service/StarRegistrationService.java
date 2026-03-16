package com.starrating.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starrating.entity.StarRegistration;
import com.starrating.mapper.StarRegistrationMapper;
import org.springframework.stereotype.Service;

@Service
public class StarRegistrationService extends ServiceImpl<StarRegistrationMapper, StarRegistration> {
}
