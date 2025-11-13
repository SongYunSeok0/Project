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
