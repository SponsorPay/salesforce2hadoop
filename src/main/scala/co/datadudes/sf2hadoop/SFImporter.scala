package co.datadudes.sf2hadoop

import java.util.Calendar

import Conversion._
import DatasetUtils._
import AvroUtils._
import com.typesafe.scalalogging.LazyLogging
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.kitesdk.data.Dataset
import com.sforce.soap.partner.sobject.SObject

class SFImporter(recordSchemas: Map[String, Schema],
                 basePath: String,
                 sfConnection: SalesforceService) extends LazyLogging {

  val fsBasePath = if(basePath.endsWith("/")) basePath else basePath + "/"

  def initialImport(recordType: String) = {
    val schema = recordSchemas(recordType)
    val dataset = initializeDataset(datasetUri(recordType), schema)

    /*
     * Some of the record fields specified in Enterprise WSDL may not
     * be visible to user under which data is extracted. If you request such
     * fields salesforce is going to return an error that some fields do not exist
     * in the record and you should check your WSDL.
     * To avoid the issue we query list of fields visible to the current user and use
     * them to extract data.
     */
    val record = sfConnection.describeObject(recordType)

    val fieldNames = record.map(_.getName)
    val results = sfConnection.query(buildSFImportQuery(recordType, fieldNames))
    storeSFRecords(results, dataset)
  }

  def incrementalImport(recordType: String, from: Calendar, until: Calendar) = {
    val schema = recordSchemas(recordType)
    val dataset = loadAndUpdateDataset(datasetUri(recordType), schema)
    val record = sfConnection.describeObject(recordType)

    val fieldList = record.map(_.getName).mkString(",")
    val results = sfConnection.getUpdated(recordType, fieldList, from, until)
    storeSFRecords(results, dataset)
  }

  private def storeSFRecords(records: Iterator[SObject], target: Dataset[GenericRecord]): Unit = {
    val writer = target.newWriter()
    val schema = target.getDescriptor.getSchema
    records.zipWithIndex.foreach { case (o, i) =>
      if(i % 2000 == 0) logger.info(s"Processed $i records")
      writer.write(sfRecord2AvroRecord(o, schema))
    }
    writer.close()
  }

  private def buildSFImportQuery(recordType: String, fields: Seq[String]): String = {
    val fieldList = fields.mkString(",")
    s"SELECT $fieldList FROM $recordType ORDER BY CreatedDate DESC"
  }

  private def datasetUri(recordType: String) = s"dataset:$fsBasePath${recordType.toLowerCase}"


}
