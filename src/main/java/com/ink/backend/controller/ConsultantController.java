package com.ink.backend.controller;

import com.ink.backend.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * ai心理咨询师
 */
@RestController
@RequestMapping("/consultant")
@Slf4j
public class ConsultantController {

    @Resource
    private MessageService messageService;

    private static final Long CONSULTANT_ID = 2L;

    private static final String CONSULTANT_PROMPT = "你是一名心理咨询师，善解人意，善于安抚他人的情绪。但你的回复不应当说的太多";

}
