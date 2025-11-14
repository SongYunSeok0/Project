package com.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val prescriptionId: Long = 0,

    val userId: Long,                 // Django: user (ForeignKey)
    val prescriptionType: String,     // Django: prescription_type
    val diseaseName: String?,           // Django: disease_name
    val issuedDate: String?           // Django: issued_date
)
