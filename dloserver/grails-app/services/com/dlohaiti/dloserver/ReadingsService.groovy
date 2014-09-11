package com.dlohaiti.dloserver
import au.com.bytecode.opencsv.CSVReader

import java.text.SimpleDateFormat

class ReadingsService {

    def incomingService
    def grailsApplication
    def messageSource

    public saveReading(params) {
        Date createdDate = params.date("createdDate", grailsApplication.config.dloserver.measurement.timeformat.toString())
        SamplingSite samplingSite = SamplingSite.findByName(params.samplingSiteName)
        if(samplingSite == null) {
          throw new MissingSamplingSiteException()
        }
        Reading reading =  Reading.findByCreatedDateAndKioskAndSamplingSite(createdDate,  params.kiosk, samplingSite)
        if(reading==null) {
            reading = new Reading([createdDate: createdDate, samplingSite: samplingSite,kiosk: params.kiosk, username: "${params.kiosk.name} operator"])
        }
        for(it in params.measurements) {
              def parameter =  Parameter.findByNameIlike(it.parameterName)
              def m=reading?.measurements.find({m -> m.parameter == parameter})
              if(m != null) {
                 m.value= new BigDecimal(it.value as Double)
                 m.save(flush: true)
              }else {
                  Measurement measurement = new Measurement()
                  measurement.parameter = parameter
                  measurement.value = new BigDecimal(it.value as Double)
                  reading.addToMeasurements(measurement)
              }
          }

        reading.save(flush: true)
        return reading
    }

    private static BigDecimal parseValue(String value) {
        switch (value.toUpperCase()) {
            case "OK": return 1
            case "NOT_OK": return 0
            default: return value.toBigDecimal()
        }
    }

    public void importIncomingFiles() {
        log.debug("Processing new incoming files")

        def incomingFiles = incomingService.incomingFiles

        if (incomingFiles?.size()) {
            log.info("Found ${incomingFiles?.size()} new files to be imported")
        }

        incomingFiles.each {
            if (importFile(it)) {
                incomingService.markAsProcessed(it)
            } else {
                incomingService.markAsFailed(it)
            }
        }

        log.debug("Finished processing ${incomingFiles.size()} files")
    }

    private boolean importFile(String filename) {
        log.info("Started importing file '$filename'")
        CSVReader reader = new CSVReader(new StringReader(incomingService.getFileContent(filename)))
        List<String[]> allLines = reader.readAll()

        List<Reading> readingsToSave = [];
        def parser = new SolarFileParser()
        if(parser.isSolarFile(allLines[0])) {
          readingsToSave = parser.parse(allLines)
        } else {
          for(String[] line in allLines.tail()) {
            if(line.length != 3) {
              log.error("Rejecting file [${filename}] because of invalid line:\n\t\t${line}")
              return false
            }

            String sensorId = line[1]
            String timestamp = line[0]
            String value = line[2]
            Date createdDate = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss").parse(timestamp)
            Sensor sensor = Sensor.findBySensorId(sensorId)
            if(sensor == null) {
              log.error("Rejecting file [${filename}] because line references invalid SensorId [${sensorId}]")
              return false
            }

            def reading = Reading.findByCreatedDateAndKioskAndSamplingSite(createdDate, sensor.kiosk, sensor.samplingSite)
            if(reading?.measurements.find({m -> m.parameter == sensor.parameter})) {
              log.warn("Ignoring duplicate line from file [${filename}]. line:\n\t\t${line}")
              continue
            }
            if(reading==null) {
                  reading = new Reading([createdDate: createdDate, samplingSite: sensor.samplingSite,kiosk:  sensor.kiosk, username: "sensor"])
            }
            Measurement measurement = new Measurement(parameter: sensor.parameter, value: parseValue(value))
            reading.addToMeasurements(measurement)
            if(reading.validate()) {
              readingsToSave.add(reading)
            } else {
              log.error("Reading from file [${filename}] with line [${line}] has validation errors preventing save")
              return false
            }
          }
        }
        for(reading in readingsToSave) {
          reading.save(flush: true)
        }
        log.info("Finished importing file [${filename}] with [${readingsToSave.size()}] readings")
        return true
    }
}
