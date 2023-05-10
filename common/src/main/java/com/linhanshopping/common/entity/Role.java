package com.linhanshopping.common.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "roles")
public class Role extends IdBasedEntity {

	@Column(name = "name", length = 40, nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 150, nullable = false)
	private String description;

	public Role() {
		super();
	}

	public Role(Integer id) {
		this.id = id;
	}

	public Role(String name) {
		this.name = name;
	}

	public Role(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
