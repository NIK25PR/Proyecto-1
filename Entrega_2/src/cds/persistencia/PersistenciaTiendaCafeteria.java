package cds.persistencia;
 
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
import cds.Club;
import cds.ReglaNegocioException;
import cds.catalogo.Alergeno;
import cds.catalogo.Articulo;
import cds.catalogo.Bebida;
import cds.catalogo.Inventario;
import cds.catalogo.ProductoCafeteria;
import cds.catalogo.Snack;
import cds.catalogo.Sugerencia;
import cds.catalogo.Ubicacion;
import cds.catalogo.Vendible;
import cds.usuarios.Empleado;
import cds.usuarios.Socio;
import cds.usuarios.Usuario;
import cds.ventas.LineaVenta;
import cds.ventas.Mensualidad;
import cds.ventas.TiendaCafeteria;
import cds.ventas.Venta;
import cds.ventas.VentaCafeteria;
import cds.ventas.VentaTienda;
 
/**
 * Guarda y carga datos/tienda_cafeteria.json.
 *
 * Las referencias entre objetos se guardan por nombre (articulos, productos,
 * ubicaciones) o por login (usuarios). Al cargar se buscan esos objetos para
 * reconstruir las referencias.
 */
public class PersistenciaTiendaCafeteria {
	public static final String ARCHIVO = "tienda_cafeteria.json";
 
 
	// ================================================================ GUARDAR
 
	public void guardar(TiendaCafeteria tienda, Path carpeta) throws IOException {
		Map<String, Object> raiz = Json.nuevoObjeto();
 
		List<Object> ubicaciones = new ArrayList<Object>();
		for (Ubicacion u : tienda.getUbicaciones()) {
			Map<String, Object> o = Json.nuevoObjeto();
			o.put("nombre", u.getNombre());
			ubicaciones.add(o);
		}
		raiz.put("ubicaciones", ubicaciones);
 
		List<Object> articulos = new ArrayList<Object>();
		for (Articulo a : tienda.getArticulos()) {
			Map<String, Object> o = Json.nuevoObjeto();
			o.put("nombre", a.getNombre());
			o.put("categoria", a.getCategoria());
			o.put("precio", a.getPrecio());
			articulos.add(o);
		}
		raiz.put("articulos", articulos);
 
		List<Object> productos = new ArrayList<Object>();
		for (ProductoCafeteria p : tienda.getProductos()) {
			Map<String, Object> o = Json.nuevoObjeto();
			o.put("tipo", p instanceof Bebida ? "BEBIDA" : "SNACK");
			o.put("nombre", p.getNombre());
			o.put("precio", p.getPrecio());
			if (p instanceof Bebida) {
				o.put("caliente", ((Bebida) p).isCaliente());
			}
			List<Object> alergenos = new ArrayList<Object>();
			for (Alergeno al : p.getAlergenos()) {
				alergenos.add(al.name());
			}
			o.put("alergenos", alergenos);
			productos.add(o);
		}
		raiz.put("productosCafeteria", productos);
 
		List<Object> inventarios = new ArrayList<Object>();
		for (Inventario i : tienda.getInventarios()) {
			Map<String, Object> o = Json.nuevoObjeto();
			o.put("articulo", i.getArticulo().getNombre());
			o.put("ubicacion", i.getUbicacion().getNombre());
			o.put("cantidad", i.getCantidad());
			inventarios.add(o);
		}
		raiz.put("inventarios", inventarios);
 
		List<Object> sugerencias = new ArrayList<Object>();
		for (Sugerencia s : tienda.getSugerencias()) {
			Map<String, Object> o = Json.nuevoObjeto();
			o.put("autor", s.getAutor().getLogin());
			o.put("descripcion", s.getDescripcion());
			o.put("fecha", Json.fecha(s.getFecha()));
			o.put("estado", s.getEstado().name());
			sugerencias.add(o);
		}
		raiz.put("sugerencias", sugerencias);
 
		List<Object> ventas = new ArrayList<Object>();
		for (Venta v : tienda.getVentas()) {
			Map<String, Object> o = Json.nuevoObjeto();
			o.put("tipo", v.getRubro().name());
			o.put("fecha", Json.fecha(v.getFecha()));
			o.put("comprador", v.getComprador().getLogin());
			List<Object> lineas = new ArrayList<Object>();
			for (LineaVenta l : v.getLineas()) {
				Map<String, Object> ol = Json.nuevoObjeto();
				ol.put("producto", l.getProducto().getNombre());
				ol.put("cantidad", l.getCantidad());
				ol.put("precioUnitario", l.getPrecioUnitario());
				lineas.add(ol);
			}
			o.put("lineas", lineas);
			o.put("puntosRedimidos", v.getPuntosRedimidos());
			o.put("confirmada", v.isConfirmada());
			if (v instanceof VentaTienda) {
				VentaTienda vt = (VentaTienda) v;
				o.put("codigoDe", vt.getDuenoCodigo() == null ? null : vt.getDuenoCodigo().getLogin());
			} else if (v instanceof VentaCafeteria) {
				o.put("porcentajePropina", ((VentaCafeteria) v).getPorcentajePropina());
			}
			ventas.add(o);
		}
		raiz.put("ventas", ventas);
 
		List<Object> mensualidades = new ArrayList<Object>();
		for (Mensualidad m : tienda.getMensualidades()) {
			Map<String, Object> o = Json.nuevoObjeto();
			o.put("socio", m.getSocio().getLogin());
			o.put("mes", m.getMes().toString());
			o.put("monto", m.getMonto());
			o.put("fechaPago", Json.fecha(m.getFechaPago()));
			mensualidades.add(o);
		}
		raiz.put("mensualidades", mensualidades);
 
		Json.guardarArchivo(carpeta.resolve(ARCHIVO), raiz);
	}
 
 
	// ================================================================ CARGAR
 
	/**
	 * @param club se usa para buscar usuarios por login (compradores, autores, socios).
	 *             Los usuarios deben estar cargados antes.
	 */
	public TiendaCafeteria cargar(Path carpeta, Club club) throws IOException, ReglaNegocioException {
		Map<String, Object> raiz = Json.objeto(Json.leerArchivo(carpeta.resolve(ARCHIVO)));
		TiendaCafeteria tienda = new TiendaCafeteria();
 
		// El orden importa: primero lo que otros objetos referencian.
		cargarUbicaciones(Json.lista(raiz, "ubicaciones"), tienda);
		cargarArticulos(Json.lista(raiz, "articulos"), tienda);
		cargarProductos(Json.lista(raiz, "productosCafeteria"), tienda);
		cargarInventarios(Json.lista(raiz, "inventarios"), tienda);
		cargarSugerencias(Json.lista(raiz, "sugerencias"), tienda, club);
		cargarVentas(Json.lista(raiz, "ventas"), tienda, club);
		cargarMensualidades(Json.lista(raiz, "mensualidades"), tienda, club);
 
		return tienda;
	}
 
	private void cargarUbicaciones(List<Object> lista, TiendaCafeteria tienda) throws ReglaNegocioException {
		for (Object elemento : lista) {
			Map<String, Object> o = Json.objeto(elemento);
			tienda.registrarUbicacion(new Ubicacion(Json.texto(o, "nombre")));
		}
	}
 
	private void cargarArticulos(List<Object> lista, TiendaCafeteria tienda) throws ReglaNegocioException {
		for (Object elemento : lista) {
			Map<String, Object> o = Json.objeto(elemento);
			tienda.registrarArticulo(new Articulo(
					Json.texto(o, "nombre"),
					Json.texto(o, "categoria"),
					Json.decimal(o, "precio")));
		}
	}
 
	private void cargarInventarios(List<Object> lista, TiendaCafeteria tienda) throws ReglaNegocioException {
		for (Object elemento : lista) {
			Map<String, Object> o = Json.objeto(elemento);
			Articulo a = tienda.buscarArticulo(Json.texto(o, "articulo"));
			Ubicacion u = tienda.buscarUbicacion(Json.texto(o, "ubicacion"));
			if (a == null || u == null) {
				throw new ReglaNegocioException("Inventario con articulo o ubicacion inexistente: " + o);
			}
			tienda.registrarInventario(new Inventario(a, u, Json.entero(o, "cantidad")));
		}
	}
 
	private void cargarProductos(List<Object> lista, TiendaCafeteria tienda) throws ReglaNegocioException {
		for (Object elemento : lista) {
			Map<String, Object> o = Json.objeto(elemento);
			String tipo = Json.texto(o, "tipo");
			String nombre = Json.texto(o, "nombre");
			double precio = Json.decimal(o, "precio");
 
			ProductoCafeteria producto;
			if (tipo.equals("BEBIDA")) {
				producto = new Bebida(nombre, precio, Json.booleano(o, "caliente"));
			} else if (tipo.equals("SNACK")) {
				producto = new Snack(nombre, precio);
			} else {
				throw new ReglaNegocioException("Tipo de producto desconocido: " + tipo);
			}
 
			for (Object texto : Json.lista(o, "alergenos")) {
				producto.agregarAlergeno(Alergeno.valueOf((String) texto));
			}
			tienda.registrarProducto(producto);
		}
	}
 
	private void cargarSugerencias(List<Object> lista, TiendaCafeteria tienda, Club club)
			throws ReglaNegocioException {
		for (Object elemento : lista) {
			Map<String, Object> o = Json.objeto(elemento);
 
			Usuario usuario = club.buscarUsuario(Json.texto(o, "autor"));
			if (!(usuario instanceof Empleado)) {
				throw new ReglaNegocioException("El autor de la sugerencia no es un empleado: " + o);
			}
 
			Sugerencia sugerencia = new Sugerencia(
					(Empleado) usuario,
					Json.texto(o, "descripcion"),
					Json.fecha(o, "fecha"));
 
			String estado = Json.texto(o, "estado");
			if (estado.equals("APROBADA")) {
				sugerencia.aprobar();
			} else if (estado.equals("RECHAZADA")) {
				sugerencia.rechazar();
			}
 
			tienda.registrarSugerencia(sugerencia);
		}
	}
 
	private void cargarVentas(List<Object> lista, TiendaCafeteria tienda, Club club)
			throws ReglaNegocioException {
		for (Object elemento : lista) {
			Map<String, Object> o = Json.objeto(elemento);
 
			Usuario comprador = club.buscarUsuario(Json.texto(o, "comprador"));
			if (comprador == null) {
				throw new ReglaNegocioException("Comprador inexistente: " + o);
			}
			LocalDate fecha = Json.fecha(o, "fecha");
			String tipo = Json.texto(o, "tipo");
 
			Venta venta;
			if (tipo.equals("TIENDA")) {
				VentaTienda vt = new VentaTienda(fecha, comprador);
				if (o.get("codigoDe") != null) {
					Usuario dueno = club.buscarUsuario(Json.texto(o, "codigoDe"));
					if (!(dueno instanceof Empleado)) {
						throw new ReglaNegocioException("El dueno del codigo no es un empleado: " + o);
					}
					vt.aplicarCodigo((Empleado) dueno);
				}
				venta = vt;
			} else if (tipo.equals("CAFETERIA")) {
				VentaCafeteria vc = new VentaCafeteria(fecha, comprador);
				vc.setPorcentajePropina(Json.decimal(o, "porcentajePropina"));
				venta = vc;
			} else {
				throw new ReglaNegocioException("Tipo de venta desconocido: " + tipo);
			}
 
			List<LineaVenta> lineas = new ArrayList<LineaVenta>();
			for (Object elemLinea : Json.lista(o, "lineas")) {
				Map<String, Object> ol = Json.objeto(elemLinea);
				String nombreProducto = Json.texto(ol, "producto");
 
				Vendible producto;
				if (tipo.equals("TIENDA")) {
					producto = tienda.buscarArticulo(nombreProducto);
				} else {
					producto = tienda.buscarProducto(nombreProducto);
				}
				if (producto == null) {
					throw new ReglaNegocioException("Producto inexistente en la venta: " + nombreProducto);
				}
 
				lineas.add(new LineaVenta(producto,
						Json.entero(ol, "cantidad"),
						Json.decimal(ol, "precioUnitario")));
			}
 
			venta.restaurar(lineas, Json.entero(o, "puntosRedimidos"), Json.booleano(o, "confirmada"));
			tienda.registrarVenta(venta);
		}
	}
 
	private void cargarMensualidades(List<Object> lista, TiendaCafeteria tienda, Club club)
			throws ReglaNegocioException {
		for (Object elemento : lista) {
			Map<String, Object> o = Json.objeto(elemento);
 
			Usuario usuario = club.buscarUsuario(Json.texto(o, "socio"));
			if (!(usuario instanceof Socio)) {
				throw new ReglaNegocioException("La mensualidad no pertenece a un socio: " + o);
			}
 
			Mensualidad mensualidad = new Mensualidad(
					(Socio) usuario,
					YearMonth.parse(Json.texto(o, "mes")),
					Json.decimal(o, "monto"));
 
			LocalDate fechaPago = Json.fecha(o, "fechaPago");
			if (fechaPago != null) {
				mensualidad.pagar(fechaPago);
			}
 
			tienda.registrarMensualidad(mensualidad);
		}
	}
 
}