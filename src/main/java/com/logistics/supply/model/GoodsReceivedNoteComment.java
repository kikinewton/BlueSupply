package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.RequestProcess;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceivedNoteComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(length = 1000)
    String description;

    boolean read;

    @Enumerated(EnumType.STRING)
    RequestProcess processWithComment;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    Employee employee;


    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "goods_received_note_id")
    GoodsReceivedNote goodsReceivedNote;

    @CreationTimestamp
    Date createdDate;

    @UpdateTimestamp
    Date updatedDate;
}
