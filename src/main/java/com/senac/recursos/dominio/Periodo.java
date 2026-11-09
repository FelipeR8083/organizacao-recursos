package com.senac.recursos.dominio;

import java.time.Instant;

/** Intervalo [inicio, fim). Intervalos que apenas se tocam NÃO se sobrepõem. */
public record Periodo(Instant inicio, Instant fim) {

    public Periodo {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Início e fim são obrigatórios");
        }
        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("O término deve ser posterior ao início");
        }
    }

    public boolean sobrepoe(Periodo outro) {
        return inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim);
    }
}
