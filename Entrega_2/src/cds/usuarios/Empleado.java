package cds.usuarios;

public abstract class Empleado extends Usuario {
	private final String codigoDescuento;


	protected Empleado(String login, String password, String nombre, String codigoDescuento) {
		super(login, password, nombre);
		this.codigoDescuento = codigoDescuento;
	}


	public String getCodigoDescuento() {
		return codigoDescuento;
	}

}
