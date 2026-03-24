package umu.pds.app.domain.ports.output;

import umu.pds.app.domain.modelo.usuario.Usuario;

import java.util.Optional;

/**
 * Puerto de salida: contrato de persistencia del Agregado Usuario.
 * El dominio declara esta interfaz; la infraestructura la implementa.
 */
public interface UsuarioRepository {

    void guardar(Usuario usuario);

    Optional<Usuario> buscarPorEmail(String email);
}
