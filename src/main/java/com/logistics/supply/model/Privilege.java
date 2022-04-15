package com.logistics.supply.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Privilege {
  @Column(length = 20)
  String name;

//  @ManyToMany(mappedBy = "privileges")
//  Collection<Role> roles;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  public Privilege(String name) {
    this.name = name;
  }
}
