import { NavLink } from 'react-router-dom'

function linkClass({ isActive }: { isActive: boolean }): string {
  return isActive ? 'nav-link active' : 'nav-link'
}

function Navbar() {
  return (
    <header className="navbar">
      <div className="navbar-inner">
        <NavLink to="/" className="brand" aria-label="Cricket Score Management home">
          <span className="brand-ball" aria-hidden="true" />
          Cricket Score
        </NavLink>
        <nav className="nav-links" aria-label="Main">
          <NavLink to="/" end className={linkClass}>
            Matches
          </NavLink>
          <NavLink to="/players" className={linkClass}>
            Players
          </NavLink>
        </nav>
      </div>
    </header>
  )
}

export default Navbar
