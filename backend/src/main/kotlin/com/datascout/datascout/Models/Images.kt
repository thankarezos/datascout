package com.datascout.datascout.models

import jakarta.persistence.*


@Entity
@Table(name = "images")
data class Image(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val userId: Long = 0,
    var path: String? = null,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "image_labels", joinColumns = [JoinColumn(name = "image_id")])
    @AttributeOverrides(
        AttributeOverride(name = "label", column = Column(name = "label")),
        AttributeOverride(name = "count", column = Column(name = "count"))
    )
    val labels: Set<Label>? = null


)

@Embeddable
data class Label(
    val label: String = "",
    val count: Int = 0
)
