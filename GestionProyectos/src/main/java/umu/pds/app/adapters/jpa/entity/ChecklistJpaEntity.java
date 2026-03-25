package umu.pds.app.adapters.jpa.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "checklists")
public class ChecklistJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String nombre;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "checklist_items", joinColumns = @JoinColumn(name = "checklist_id"))
    @OrderColumn(name = "item_orden")
    private List<ItemChecklistJpaEmbeddable> items = new ArrayList<>();

    public ChecklistJpaEntity() {}

    public ChecklistJpaEntity(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public List<ItemChecklistJpaEmbeddable> getItems() { return items; }
    public void setItems(List<ItemChecklistJpaEmbeddable> items) { this.items = items; }
}
