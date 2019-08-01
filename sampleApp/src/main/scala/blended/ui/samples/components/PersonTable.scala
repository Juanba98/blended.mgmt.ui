package blended.ui.samples.components

import blended.ui.components.reacttable.ReactTable
import blended.ui.samples.state.Person

object PersonTable extends ReactTable[Person] {

  val props = TableProperties(
    columns = Seq(
      ColumnConfig(name = "first", renderer = defaultCellRenderer(_.first),label = "Name"),
      ColumnConfig(name = "last", renderer = defaultCellRenderer(_.last),label = "Name"),
      ColumnConfig(name = "age", renderer = defaultCellRenderer(_.age.toString), numeric = true, width = Some("15%"),label = "Name"),
      ColumnConfig(name = "eMail", renderer = eMailRenderer(_.eMail),label = "Name")
    )
  )
}
