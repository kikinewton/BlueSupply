package com.logistics.supply.controller;

import com.logistics.supply.service.RequestItemCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    final RequestItemCommentService requestItemCommentService;



}
