package com.nativa.vecino_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.nativa.vecino_service.model.Rol;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VecinoRequest {
    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}-[0-9Kk]$", message = "El RUT debe tener formato 12345678-9")
    private String rut;

    @NotBlank(message = "El correo es obligatorio")
    private String correoVecino;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombres;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellidos;

    @NotBlank(message = "El telefono es obligatorio")
    private String telefonoVecino;

    private Rol rol;

    private Boolean estadoVecino;

}
