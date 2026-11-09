package hu.bme.backend.entities;

import hu.bme.backend.entities.common.Base;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stores")
public class Store extends Base {

    @NotBlank
    @Getter @Setter
    @Column(nullable = false, unique = true, name = "name")
    private String name;

}
