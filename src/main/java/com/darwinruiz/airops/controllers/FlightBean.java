package com.darwinruiz.airops.controllers;

import com.darwinruiz.airops.models.Aircraft;
import com.darwinruiz.airops.models.Flight;
import com.darwinruiz.airops.models.Pilot;
import com.darwinruiz.airops.services.CatalogService;
import com.darwinruiz.airops.services.FlightService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import java.io.Serializable;
import java.util.*;

@Named
@ViewScoped
public class FlightBean implements Serializable {

    @Inject
    CatalogService catalog;

    @Inject
    FlightService flights;

    @Inject
    private Validator validator;

    private Flight flight;
    private ScheduleModel schedule;
    private boolean dialogVisible;
    private Flight selected;
    private boolean detailVisible;

    @PostConstruct
    public void init() {
        flight = new Flight();
        schedule = new DefaultScheduleModel();
        dialogVisible = false;
        reloadSchedule();
    }

    /** Crear nuevo vuelo **/
    public void newFlight() {
        clearFacesMessages();
        this.flight = new Flight();
        this.flight.setPilot(null);
        this.flight.setAircraft(null);
        this.dialogVisible = true;
    }

    /** Editar vuelo existente **/
    public void edit(Flight f) {
        clearFacesMessages();

        // Clonamos el vuelo seleccionado para no modificar la lista directamente
        this.flight = f;

        // Asignar las instancias correctas de piloto y aeronave desde el catálogo
        if (f.getPilot() != null) {
            this.flight.setPilot(
                    catalog.pilots().stream()
                            .filter(p -> p.getId().equals(f.getPilot().getId()))
                            .findFirst()
                            .orElse(null)
            );
        }

        if (f.getAircraft() != null) {
            this.flight.setAircraft(
                    catalog.aircraft().stream()
                            .filter(a -> a.getId().equals(f.getAircraft().getId()))
                            .findFirst()
                            .orElse(null)
            );
        }

        // Mostrar diálogo
        this.dialogVisible = true;
    }


    /** Eliminar vuelo **/
    public void delete(Flight f) {
        try {
            flights.delete(f);
            reloadSchedule();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Vuelo eliminado", "Operación exitosa"));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar vuelo", ex.getMessage()));
        }
    }

    /** Evento al seleccionar en el calendario **/
    public void onEventSelect(SelectEvent<ScheduleEvent<?>> e) {
        Object data = e.getObject().getData();
        if (data instanceof Flight f) {
            this.selected = f;
            this.detailVisible = true;
        }
    }

    /** Guardar vuelo **/
    public void save() {
        Set<ConstraintViolation<Flight>> violations = validator.validate(flight);

        if (!violations.isEmpty()) {
            for (ConstraintViolation<Flight> violation : violations) {
                String field = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                String label = getFieldLabel(field);

                FacesContext.getCurrentInstance().addMessage("frmFlights:msgFlight",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                label + ": " + message, null));
            }

            FacesContext.getCurrentInstance().validationFailed();
            return;
        }

        flights.save(flight);
        reloadSchedule();
        this.dialogVisible = false;
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Vuelo guardado", "Operación exitosa"));
        this.flight = new Flight();
    }

    /** Recargar calendario **/
    public void reloadSchedule() {
        DefaultScheduleModel model = new DefaultScheduleModel();
        for (Flight f : flights.flights()) {
            DefaultScheduleEvent<Flight> ev = DefaultScheduleEvent.<Flight>builder()
                    .title(f.getFlightNumber() + " • " + f.getOriginIata() + "→" + f.getDestinationIata())
                    .startDate(f.getDeparture())
                    .endDate(f.getArrival())
                    .data(f)
                    .build();
            model.addEvent(ev);
        }
        this.schedule = model;
    }

    /** Limpiar mensajes **/
    private void clearFacesMessages() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) return;
        for (Iterator<FacesMessage> it = ctx.getMessages(); it.hasNext(); ) {
            it.next();
            it.remove();
        }
    }

    /** Etiquetas amigables para los mensajes **/
    private String getFieldLabel(String fieldName) {
        Map<String, String> labels = new HashMap<>();
        labels.put("flightNumber", "Número de vuelo");
        labels.put("originIata", "Origen (IATA)");
        labels.put("destinationIata", "Destino (IATA)");
        labels.put("departure", "Salida");
        labels.put("arrival", "Llegada");
        labels.put("passengers", "Pasajeros");
        labels.put("pilot", "Piloto");
        labels.put("aircraft", "Aeronave");
        labels.put("createdAt", "Fecha de creación");
        labels.put("updatedAt", "Última actualización");
        labels.put("active", "Activo");

        return labels.getOrDefault(fieldName, fieldName);
    }

    // 🔹 Getters y setters
    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public ScheduleModel getSchedule() {
        return schedule;
    }

    public boolean isDialogVisible() {
        return dialogVisible;
    }

    public void setDialogVisible(boolean dialogVisible) {
        this.dialogVisible = dialogVisible;
    }

    public List<Pilot> getPilots() {
        return catalog.pilots();
    }

    public List<Aircraft> getAircraft() {
        return catalog.aircraft();
    }

    public List<Flight> getFlights() {
        return flights.flights();
    }

    public Flight getSelected() {
        return selected;
    }

    public boolean isDetailVisible() {
        return detailVisible;
    }

    public void setDetailVisible(boolean b) {
        this.detailVisible = b;
    }
}

