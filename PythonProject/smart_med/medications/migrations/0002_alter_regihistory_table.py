from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ("medications", "0001_initial"),
    ]

    operations = [
        migrations.AlterModelTable(
            name="regihistory",
            table="regihistory",
        ),
    ]
