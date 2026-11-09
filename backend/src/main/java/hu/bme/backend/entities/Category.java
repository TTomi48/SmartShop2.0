package hu.bme.backend.entities;

import hu.bme.backend.entities.common.Base;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category extends Base {

    @NotBlank
    @Column(nullable = false, unique = true, name = "name")
    private String name;
}
