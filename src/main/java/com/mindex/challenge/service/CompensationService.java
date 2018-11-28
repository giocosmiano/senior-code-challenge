package com.mindex.challenge.service;

import com.mindex.challenge.dto.CompensationDto;

import java.util.List;

public interface CompensationService {
    CompensationDto save(CompensationDto compensationDto);
    CompensationDto readById(String id);
    List<CompensationDto> readAll();
}
