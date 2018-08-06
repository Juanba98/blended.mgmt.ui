package blended.ui.samples.compoments

import blended.material.ui.Icons.MenuIcon
import blended.ui.material.MaterialUI._
import blended.ui.samples.state.SampleAppState
import blended.ui.samples.theme.AppBarStyles
import com.github.ahnfelt.react4s._

case class AppBarComponent(s : P[SampleAppState]) extends Component[NoEmit] {

  override def render(get: Get): Node = AppBar(
    AppBarStyles.root,
    Toolbar(
      IconButton(
        AppBarStyles.menuButton,
        J("color", "inherit"),
        J("aria-label", "Menu"),
        MenuIcon()
      ),
      Typography(
        AppBarStyles.flex,
        J("variant", "title"), J("color", "inherit"),
        Text("Blended Component Samples")
      ),
      Button(J("color", "inherit"), Text("Login"))
    )
  )
}