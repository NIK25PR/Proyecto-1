package cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;

import cds.Club;
import cds.academias.Instalacion;
import cds.academias.TipoInstalacion;
import cds.catalogo.Articulo;
import cds.catalogo.Bebida;
import cds.catalogo.Inventario;
import cds.catalogo.ProductoCafeteria;
import cds.catalogo.Sugerencia;
import cds.catalogo.Ubicacion;
import cds.persistencia.PersistenciaTiendaCafeteria;
import cds.usuarios.Entrenador;
import cds.usuarios.Socio;
import cds.ventas.LineaVenta;
import cds.ventas.Mensualidad;
import cds.ventas.TiendaCafeteria;
import cds.ventas.Venta;
import cds.ventas.VentaCafeteria;
import cds.ventas.VentaTienda;

/**
 * Demostracion del modulo 6: tienda, cafeteria, inventario, mensualidades y persistencia.
 * Lee los datos de prueba de datos/tienda_cafeteria.json.
 */
public class DemoTiendaCafeteria {
	private static final LocalDate HOY = LocalDate.of(2026, 10, 18);

	private Club club;
	private TiendaCafeteria tienda;
	private Socio ana;
	private Socio carlos;
	private Entrenador leo;


	public static void main(String[] args) {
		new DemoTiendaCafeteria().ejecutar(Paths.get(args.length > 0 ? args[0] : "datos"));
	}

	public void ejecutar(Path carpeta) {
		Pantalla.titulo("DEMO TIENDA Y CAFETERIA");
		if (!Files.exists(carpeta.resolve(PersistenciaTiendaCafeteria.ARCHIVO))) {
			Pantalla.info("No se encontro " + carpeta.resolve(PersistenciaTiendaCafeteria.ARCHIVO).toAbsolutePath());
			Pantalla.info("Ejecute el programa desde la carpeta Entrega_2.");
			return;
		}

		prepararUsuarios();
		Pantalla.seccion("1. Cargar datos de prueba");
		Pantalla.esperarExito("Cargar " + PersistenciaTiendaCafeteria.ARCHIVO, () -> {
			tienda = new PersistenciaTiendaCafeteria().cargar(carpeta, club);
		});
		if (tienda == null) {
			return;
		}
		mostrarCatalogo();
		demostrarTraslados();
		demostrarVentaTienda();
		demostrarVentaCafeteria();
		demostrarPuntos();
		demostrarMensualidades();
		demostrarSugerencias();
		demostrarPersistencia(carpeta.resolve("salida_demo"));
		Pantalla.resumen();
	}


	private void prepararUsuarios() {
		// Los usuarios se crean aqui porque su persistencia todavia no esta implementada.
		club = new Club();
		ana = new Socio("ana", "ana123", "Ana Gomez", "1010", LocalDate.of(2001, 3, 14));
		carlos = new Socio("carlos", "carlos123", "Carlos Ruiz", "2020", LocalDate.of(1998, 7, 2));
		leo = new Entrenador("leo", "leo123", "Leonardo Diaz", "LEO-CDS");
		Pantalla.esperarExito("Registrar usuarios de la demo (ana, carlos, leo)", () -> {
			club.registrarUsuario(ana);
			club.registrarUsuario(carlos);
			club.registrarUsuario(leo);
		});
	}

	private void mostrarCatalogo() {
		Pantalla.seccion("2. Catalogo e inventario");
		Pantalla.info(String.format("%-26s %-10s %12s %8s %8s", "Articulo", "Categoria", "Precio", "Bodega", "Tienda"));
		for (Articulo a : tienda.getArticulos()) {
			Pantalla.info(String.format("%-26s %-10s %12s %8d %8d", a.getNombre(), a.getCategoria(),
					Pantalla.pesos(a.getPrecio()), stock(a, "Bodega"), stock(a, "Tienda")));
		}
		Pantalla.info("");
		Pantalla.info(String.format("%-20s %10s  %-8s %s", "Producto cafeteria", "Precio", "Tipo", "Alergenos"));
		for (ProductoCafeteria p : tienda.getProductos()) {
			String tipo = p instanceof Bebida ? (p.esBebidaCaliente() ? "caliente" : "fria") : "snack";
			Pantalla.info(String.format("%-20s %10s  %-8s %s", p.getNombre(), Pantalla.pesos(p.getPrecio()), tipo,
					p.getAlergenos().isEmpty() ? "-" : p.getAlergenos().toString()));
		}
	}

	private int stock(Articulo a, String ubicacion) {
		Inventario i = tienda.buscarInventario(a, tienda.buscarUbicacion(ubicacion));
		return i == null ? 0 : i.getCantidad();
	}

	private void demostrarTraslados() {
		Pantalla.seccion("3. Traslados de inventario (HU-ADM-03)");
		Articulo balon = tienda.buscarArticulo("Balon de futbol");
		Ubicacion bodega = tienda.buscarUbicacion("Bodega");
		Ubicacion local = tienda.buscarUbicacion("Tienda");
		int antesBodega = stock(balon, "Bodega");
		int antesTienda = stock(balon, "Tienda");
		Pantalla.esperarExito("Trasladar 5 balones de Bodega a Tienda", () -> tienda.trasladar(balon, bodega, local, 5));
		Pantalla.verificar("Bodega: " + antesBodega + " -> " + stock(balon, "Bodega")
				+ ", Tienda: " + antesTienda + " -> " + stock(balon, "Tienda"),
				stock(balon, "Bodega") == antesBodega - 5 && stock(balon, "Tienda") == antesTienda + 5);
		Pantalla.esperarRechazo("Trasladar 999 balones (no hay suficientes)",
				() -> tienda.trasladar(balon, bodega, local, 999));
		Pantalla.esperarRechazo("Trasladar a la misma ubicacion", () -> tienda.trasladar(balon, bodega, bodega, 1));
	}

	private void demostrarVentaTienda() {
		Pantalla.seccion("4. Venta en la tienda: IVA 19% y codigo de descuento");
		Articulo balon = tienda.buscarArticulo("Balon de futbol");
		Articulo camiseta = tienda.buscarArticulo("Camiseta del club");
		Ubicacion local = tienda.buscarUbicacion("Tienda");

		VentaTienda venta = new VentaTienda(HOY, ana);
		int stockAntes = stock(balon, "Tienda");
		Pantalla.esperarExito("Ana compra 2 balones y 1 camiseta con el codigo de Leo (8%)", () -> {
			venta.agregarLinea(balon, 2);
			venta.agregarLinea(camiseta, 1);
			venta.aplicarCodigo(leo);
			venta.descontarInventario(tienda.inventariosEn(local));
			venta.confirmar();
			tienda.registrarVenta(venta);
		});
		imprimirRecibo(venta);
		Pantalla.verificar("Descuento del 8% por codigo compartido",
				Math.abs(venta.descuento() - venta.subtotal() * 0.08) < 0.01);
		Pantalla.verificar("IVA del 19% sobre el valor con descuento",
				Math.abs(venta.impuesto() - (venta.subtotal() - venta.descuento()) * 0.19) < 0.01);
		Pantalla.verificar("Stock de balones en Tienda: " + stockAntes + " -> " + stock(balon, "Tienda"),
				stock(balon, "Tienda") == stockAntes - 2);

		VentaTienda propia = new VentaTienda(HOY, leo);
		Pantalla.esperarExito("Leo usa su propio codigo (15%)", () -> {
			propia.agregarLinea(camiseta, 1);
			propia.aplicarCodigo(leo);
			propia.descontarInventario(tienda.inventariosEn(local));
			propia.confirmar();
			tienda.registrarVenta(propia);
		});
		Pantalla.verificar("Descuento del 15% para el dueno del codigo: " + Pantalla.pesos(propia.descuento()),
				Math.abs(propia.descuento() - propia.subtotal() * 0.15) < 0.01);

		VentaTienda otra = new VentaTienda(HOY, carlos);
		Pantalla.esperarRechazo("Vender un producto de cafeteria en la tienda",
				() -> otra.agregarLinea(tienda.buscarProducto("Tinto"), 1));
		Pantalla.esperarRechazo("Aplicar dos codigos de descuento (no acumulable)", () -> {
			otra.aplicarCodigo(leo);
			otra.aplicarCodigo(leo);
		});
		Pantalla.esperarRechazo("Comprar 500 camisetas (no hay stock en Tienda)", () -> {
			otra.agregarLinea(camiseta, 500);
			otra.descontarInventario(tienda.inventariosEn(local));
		});
		Pantalla.esperarRechazo("Registrar una venta sin confirmar", () -> tienda.registrarVenta(otra));
	}

	private void demostrarVentaCafeteria() {
		Pantalla.seccion("5. Cafeteria: impoconsumo 8%, propina, alergenos y RN-20");
		VentaCafeteria venta = new VentaCafeteria(HOY, carlos);
		Pantalla.esperarExito("Carlos pide un Latte y un Brownie", () -> {
			venta.agregarLinea(tienda.buscarProducto("Latte"), 1);
			venta.agregarLinea(tienda.buscarProducto("Brownie"), 1);
		});
		Pantalla.info("Alergenos informados antes de comprar: " + venta.alergenos());
		Pantalla.verificar("Propina sugerida del 10%", venta.getPorcentajePropina() == 0.10);
		Pantalla.esperarExito("Carlos cambia la propina al 5%", () -> venta.setPorcentajePropina(0.05));
		Pantalla.esperarRechazo("Propina negativa", () -> venta.setPorcentajePropina(-0.1));

		Instalacion padel = new Instalacion("Padel 1", TipoInstalacion.CANCHA_PADEL, false);
		Instalacion mesaTechada = new Instalacion("Mesa 1", TipoInstalacion.MESA_TENIS_MESA, true);
		Instalacion mesaAbierta = new Instalacion("Mesa 2", TipoInstalacion.MESA_TENIS_MESA, false);
		Pantalla.esperarRechazo("Despachar bebida caliente a un socio en cancha de padel",
				() -> venta.validarDespacho(padel));
		Pantalla.esperarRechazo("Despachar bebida caliente a un socio en mesa de tenis de mesa techada",
				() -> venta.validarDespacho(mesaTechada));
		Pantalla.esperarExito("Despachar en una mesa al aire libre", () -> venta.validarDespacho(mesaAbierta));
		Pantalla.esperarExito("Confirmar y registrar la venta", () -> {
			venta.confirmar();
			tienda.registrarVenta(venta);
		});
		imprimirRecibo(venta);
		Pantalla.verificar("Impoconsumo del 8% sobre el subtotal",
				Math.abs(venta.impuesto() - venta.subtotal() * 0.08) < 0.01);

		VentaCafeteria fria = new VentaCafeteria(HOY, ana);
		Pantalla.esperarExito("Una bebida fria si se puede despachar en padel", () -> {
			fria.agregarLinea(tienda.buscarProducto("Jugo de naranja"), 1);
			fria.validarDespacho(padel);
		});
		Pantalla.esperarRechazo("Vender un articulo de la tienda en la cafeteria",
				() -> fria.agregarLinea(tienda.buscarArticulo("Balon de futbol"), 1));
	}

	private void demostrarPuntos() {
		Pantalla.seccion("6. Puntos de fidelidad (2% de cada compra)");
		for (Venta v : tienda.getVentas()) {
			Pantalla.info(String.format("%-10s %-9s total %12s -> genera %6d puntos", v.getComprador().getLogin(),
					v.getRubro(), Pantalla.pesos(v.total()), v.puntosGenerados()));
		}
		Venta ultima = tienda.getVentas().get(0);
		Pantalla.verificar("Los puntos son el 2% del total", ultima.puntosGenerados() == (int) Math.floor(ultima.total() * 0.02));
		VentaTienda nueva = new VentaTienda(HOY, carlos);
		Pantalla.esperarRechazo("Carlos intenta redimir 1.000 puntos que no tiene",
				() -> nueva.redimirPuntos(1000));
		Pantalla.info("Nota: acumular puntos en el usuario requiere los metodos que faltan en Usuario.");
	}

	private void demostrarMensualidades() {
		Pantalla.seccion("7. Mensualidades");
		Mensualidad octubre = new Mensualidad(ana, YearMonth.of(2026, 10), 150000);
		Mensualidad noviembre = new Mensualidad(ana, YearMonth.of(2026, 11), 150000);
		tienda.registrarMensualidad(octubre);
		tienda.registrarMensualidad(noviembre);
		Pantalla.esperarExito("Ana paga la mensualidad de octubre", () -> octubre.pagar(HOY));
		Pantalla.esperarRechazo("Ana intenta pagar octubre otra vez", () -> octubre.pagar(HOY));
		for (Mensualidad m : tienda.getMensualidades()) {
			Pantalla.info(String.format("%-8s %s %10s  %s", m.getSocio().getLogin(), m.getMes(),
					Pantalla.pesos(m.getMonto()), m.isPagada() ? "pagada el " + m.getFechaPago() : "PENDIENTE"));
		}
	}

	private void demostrarSugerencias() {
		Pantalla.seccion("8. Sugerencias de empleados");
		Sugerencia s = new Sugerencia(leo, "Agregar bebida isotonica a la cafeteria", HOY);
		tienda.registrarSugerencia(s);
		Pantalla.verificar("La sugerencia nace PENDIENTE", s.getEstado().name().equals("PENDIENTE"));
		Pantalla.esperarExito("El administrador la aprueba", s::aprobar);
		Pantalla.esperarRechazo("Rechazar una sugerencia ya aprobada", s::rechazar);
	}

	private void demostrarPersistencia(Path salida) {
		Pantalla.seccion("9. Persistencia: guardar y volver a cargar");
		PersistenciaTiendaCafeteria p = new PersistenciaTiendaCafeteria();
		Pantalla.esperarExito("Guardar en " + salida, () -> p.guardar(tienda, salida));
		Pantalla.esperarExito("Volver a cargar desde " + salida, () -> {
			TiendaCafeteria copia = p.cargar(salida, club);
			Pantalla.verificar("Ventas: " + tienda.getVentas().size() + " guardadas, " + copia.getVentas().size() + " cargadas",
					copia.getVentas().size() == tienda.getVentas().size());
			Pantalla.verificar("Mensualidades: " + copia.getMensualidades().size(),
					copia.getMensualidades().size() == tienda.getMensualidades().size());
			Articulo balon = copia.buscarArticulo("Balon de futbol");
			int original = stock(tienda.buscarArticulo("Balon de futbol"), "Tienda");
			int cargado = copia.buscarInventario(balon, copia.buscarUbicacion("Tienda")).getCantidad();
			Pantalla.verificar("Stock de balones en Tienda se conserva: " + cargado, cargado == original);
			double totalOriginal = tienda.getVentas().get(0).total();
			double totalCargado = copia.getVentas().get(0).total();
			Pantalla.verificar("El total de la primera venta se conserva: " + Pantalla.pesos(totalCargado),
					Math.abs(totalOriginal - totalCargado) < 0.01);
		});
	}

	private void imprimirRecibo(Venta v) {
		Pantalla.info("+------------------------------------------------+");
		Pantalla.info(String.format("| RECIBO %-9s %-15s %12s |", v.getRubro(), v.getComprador().getLogin(), v.getFecha()));
		for (LineaVenta l : v.getLineas()) {
			Pantalla.info(String.format("| %2d x %-27s %12s |", l.getCantidad(), l.getProducto().getNombre(),
					Pantalla.pesos(l.subtotal())));
		}
		Pantalla.info(String.format("| %-32s %12s |", "Subtotal", Pantalla.pesos(v.subtotal())));
		if (v.descuento() > 0) {
			Pantalla.info(String.format("| %-32s %12s |", "Descuento", "-" + Pantalla.pesos(v.descuento())));
		}
		Pantalla.info(String.format("| %-32s %12s |", v instanceof VentaTienda ? "IVA 19%" : "Impoconsumo 8%",
				Pantalla.pesos(v.impuesto())));
		if (v.cargosAdicionales() > 0) {
			Pantalla.info(String.format("| %-32s %12s |", "Propina", Pantalla.pesos(v.cargosAdicionales())));
		}
		Pantalla.info(String.format("| %-32s %12s |", "TOTAL", Pantalla.pesos(v.total())));
		Pantalla.info(String.format("| %-32s %12d |", "Puntos generados", v.puntosGenerados()));
		Pantalla.info("+------------------------------------------------+");
	}

}
