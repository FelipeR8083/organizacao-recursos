package com.senac.recursos.dominio;

import java.util.EnumSet;
import java.util.Set;

public enum EstadoReserva {
    SOLICITADA, APROVADA, EM_USO, CONCLUIDA, REJEITADA, CANCELADA, NAO_COMPARECEU;

    public Set<EstadoReserva> proximosPermitidos() {
        return switch (this) {
            case SOLICITADA -> EnumSet.of(APROVADA, REJEITADA, CANCELADA);
            case APROVADA -> EnumSet.of(EM_USO, CANCELADA, NAO_COMPARECEU);
            case EM_USO -> EnumSet.of(CONCLUIDA);
            default -> EnumSet.noneOf(EstadoReserva.class);
        };
    }

    public boolean podeIrPara(EstadoReserva destino) {
        return proximosPermitidos().contains(destino);
    }

    /** Estados que ocupam o recurso no horário (bloqueiam novas reservas). */
    public static Set<EstadoReserva> ativos() {
        return EnumSet.of(SOLICITADA, APROVADA, EM_USO);
    }
}
