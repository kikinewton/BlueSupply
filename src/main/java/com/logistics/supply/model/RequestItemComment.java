package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.RequestProcess;
import com.logistics.supply.event.RequestItemCommentListener;
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
@EntityListeners(RequestItemCommentListener.class)
public class RequestItemComment {
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
    @JoinColumn(name = "request_item_id")
    RequestItem requestItem;

    @CreationTimestamp
    Date createdDate;

    @UpdateTimestamp
    Date updatedDate;
}
