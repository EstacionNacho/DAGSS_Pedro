/*
 Proyecto Java EE, DAGSS-2013
 */
package es.uvigo.esei.dagss.controladores.farmacia;

import es.uvigo.esei.dagss.controladores.autenticacion.AutenticacionControlador;
import es.uvigo.esei.dagss.dominio.daos.FarmaciaDAO;
import es.uvigo.esei.dagss.dominio.entidades.Farmacia;
import es.uvigo.esei.dagss.dominio.entidades.TipoUsuario;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

//Imports nuevos
import es.uvigo.esei.dagss.dominio.entidades.Paciente;
import es.uvigo.esei.dagss.dominio.entidades.Prescripcion;
import es.uvigo.esei.dagss.dominio.entidades.Receta;
import es.uvigo.esei.dagss.dominio.daos.PacienteDAO;
import es.uvigo.esei.dagss.dominio.daos.PrescripcionDAO;
import es.uvigo.esei.dagss.dominio.daos.RecetaDAO;
import java.text.ParseException;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ribadas
 */
@Named(value = "farmaciaControlador")
@SessionScoped
public class FarmaciaControlador implements Serializable {

    private Farmacia farmaciaActual;
    private String nif;
    private String password;

     //Atributos nuevos
    private Paciente pacienteActual;
    private List<Receta> recetas;
    private List<Prescripcion> prescripciones;
    private String numeroTarjetaSanitaria;
    private Receta recetaActual;
    
    @Inject
    private AutenticacionControlador autenticacionControlador;

    @EJB
    private FarmaciaDAO farmaciaDAO;
    
    //Injects nuevos
    @Inject
    private RecetaDAO recetaDAO;
    @Inject
    private PrescripcionDAO prescripcionDAO;
    @Inject
    private PacienteDAO pacienteDAO;

    /**
     * Creates a new instance of AdministradorControlador
     */
    public FarmaciaControlador() {
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Farmacia getFarmaciaActual() {
        return farmaciaActual;
    }

    public void setFarmaciaActual(Farmacia farmaciaActual) {
        this.farmaciaActual = farmaciaActual;
    }

    private boolean parametrosAccesoInvalidos() {
        return ((nif == null) || (password == null));
    }

    public String doLogin() {
        String destino = null;
        if (parametrosAccesoInvalidos()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No se ha indicado un nif o una contraseña", ""));
        } else {
            Farmacia farmacia = farmaciaDAO.buscarPorNIF(nif);
            if (farmacia == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe una farmacia con el NIF " + nif, ""));
            } else {
                if (autenticacionControlador.autenticarUsuario(farmacia.getId(), password,
                        TipoUsuario.FARMACIA.getEtiqueta().toLowerCase())) {
                    farmaciaActual = farmacia;
                    destino = "privado/index";
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Credenciales de acceso incorrectas", ""));
                }

            }
        }
        return destino;
    }
    
     public String gotoRecetas(){
         
        String destino = "BuscarReceta";
        Paciente paciente = pacienteDAO.buscarPorTarjetaSanitaria(numeroTarjetaSanitaria);

        if (paciente == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No existe un paciente con targeta sanitaria: " + numeroTarjetaSanitaria, ""));
        } else {
            pacienteActual = paciente;
            recetas = recetaDAO.buscarPorIdPacienteConPrescripcion(pacienteActual.getId());
            destino = "listaRecetas";
        }
         
        return destino;
    }
     
    public String comprobarSituacion(Date inicio, Date fin){
       
        Date actual = new Date();
        String toRet = "No disponible";
        
        if(actual.before(fin) && actual.after(inicio)){
            toRet = "Disponible para suministro";
        }
        
        return toRet;
    }
    
    public String servirReceta(Receta receta){
        
        String destino = "listaRecetas";
        Date actual = new Date();
        
        if(receta.getEstadoReceta().getEtiqueta().equals("GENERADA") && actual.before(receta.getFinValidez()) && actual.after(receta.getInicioValidez())){
            recetaActual = receta;
            destino = "ServirReceta";
        }
        
        return destino;
    }
     
     public String doShowReceta(List<Receta> recetas){
        this.recetas=recetas;
        return "detalleReceta";
    }
     
     public Receta getReceta(){
         return recetaActual;
     }
     
     public void setReceta(Receta receta){
         this.recetaActual = receta;
     }
    
    public List<Receta> getRecetas(){
        return recetas;
    }
    public List<Prescripcion> getPrescripciones(){
        return prescripciones;
    }
    
    public String getNumeroTarjetaSanitaria() {
        return numeroTarjetaSanitaria;
    }
    
    public void setNumeroTarjetaSanitaria(String numeroTarjetaSanitaria) {
        this.numeroTarjetaSanitaria = numeroTarjetaSanitaria;
    }
    
    public Paciente getPaciente(){
        return pacienteActual;
    }

     public void doActualizarReceta(Receta receta){
        recetaDAO.actualizar(receta);
    }
}