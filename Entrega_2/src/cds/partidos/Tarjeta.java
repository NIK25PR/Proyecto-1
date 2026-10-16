package cds.partidos;

public class Tarjeta {
	private final TipoTarjeta tipo;


	public Tarjeta (TipoTarjeta tipo) {
		this.tipo = tipo;
	}


	public TipoTarjeta getTipo() {
		return tipo;
	}

}
