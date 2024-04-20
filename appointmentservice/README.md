# Build or Update the service's Docker image using the following command (Buildpacks):

```bash
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=appointmentservice
```

# Document Structure (Appointments):

```json
{
    "_id": {
        "$type": "ObjectId"
    },
    "doctorId": {
        "$type": "ObjectId",
        "required": true
    },
    "patientId": {
        "$type": "ObjectId",
        "required": true
    },
    "serviceType": {
        "type": "string",
        "required": true
    },
    "dateTime": {
        "type": "date",
        "required": true
    },
    "endDateTime": {
        "type": "date",
        "required": true
    },
    "status": {
        "type": "string",
        "required": true,
        "enum": [
            "pending",
            "confirmed",
            "cancelled"
        ]
    },
    "details": {
        "type": "string"
    },
    "payment": {
        "type": "object",
        "properties": {
            "method": {
                "type": "string",
                "enum": [
                    "online",
                    "physical"
                ]
            },
            "status": {
                "type": "string",
                "enum": [
                    "pending",
                    "paid",
                    "failed"
                ]
            },
            "paymentid": {
                "type": "objectid"
            },
            "details": {
                "type": "object",
                "properties": {
                    "receivedby": {
                        "type": "string"
                    },
                    "amountpaid": {
                        "type": "number"
                    },
                    "note": {
                        "type": "string"
                    }
                }
            }
        }
    }
}
```
