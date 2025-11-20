from django.db import models
from pgvector.django import VectorField

EMB_DIM = 384

class Chunk(models.Model):
    chunk_id = models.CharField(max_length=128, unique=True)
    item_name = models.CharField(max_length=1024, db_index=True)
    section = models.CharField(max_length=64, db_index=True)
    chunk_index = models.IntegerField()
    text = models.TextField()
    embedding = VectorField(dimensions=EMB_DIM)

    def __str__(self):
        return f"{self.item_name} ({self.section}#{self.chunk_index})"

class HealthFood(models.Model):
    manufacturer = models.CharField("제조사", max_length=200)
    product_name = models.CharField("제품명", max_length=300)
    serve_use = models.TextField("섭취 방법", null=True, blank=True)
    intake_hint = models.TextField("주의사항", null=True, blank=True)
    main_function = models.TextField("기능성", null=True, blank=True)

    class Meta:
        unique_together = ("manufacturer", "product_name")
        indexes = [
            models.Index(fields=["manufacturer"]),
            models.Index(fields=["product_name"]),
        ]

    def __str__(self):
        return f"{self.manufacturer} - {self.product_name}"