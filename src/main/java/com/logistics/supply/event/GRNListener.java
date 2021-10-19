package com.logistics.supply.event;

import com.logistics.supply.model.GoodsReceivedNote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;

@Slf4j
@RequiredArgsConstructor
public class GRNListener {

    @PostPersist
    public void sendEmail(GoodsReceivedNote goodsReceivedNote) {

    }
}
