package cds.academias;

public class Instalacion {
	
	private String nombre;
	private TipoInstalacion tipo;
	private boolean techada;
	
	public Instalacion(String nombre, TipoInstalacion tipo, boolean techada) {
		this.nombre = nombre;
		this.tipo = tipo;
		this.techada = techada;
		
	}
	
	public int capacidadMaxima(Modalidad modalidad) {
		if(tipo == TipoInstalacion.CANCHA_TENIS) {
			if(modalidad == Modalidad.DOBLES) {
				return 4;
			}else {
				return 2;
			}
		}else if(tipo == TipoInstalacion.CANCHA_PADEL) {
			return 4;
		}else if(tipo == TipoInstalacion.MESA_TENIS_MESA) {
			if(modalidad == Modalidad.DOBLES) {
				return 4;
			}else {
				return 2;
			}
		}else {
			return 0;
		}
	}
	
	public String getNombre() {
		return nombre;
		
	}
	
	public TipoInstalacion getTipo() {
		return tipo;
		
	}
	
	public boolean isTechada() {
		return techada;
	}

	public boolean restringeBebidaCaliente() {
		return tipo == TipoInstalacion.CANCHA_PADEL
				|| (tipo == TipoInstalacion.MESA_TENIS_MESA && techada);
	}

}
