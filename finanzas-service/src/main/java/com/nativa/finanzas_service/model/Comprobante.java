package com.nativa.finanzas_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comprobante")
@Data
@NoArgsConstructor
public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pago_id", nullable = false, unique = true)
    private Pago pago;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;
}
