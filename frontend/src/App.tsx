import { Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar.tsx'
import StateMessage from './components/StateMessage.tsx'
import HomePage from './pages/HomePage.tsx'
import MatchPage from './pages/MatchPage.tsx'
import PlayerPage from './pages/PlayerPage.tsx'
import PlayersPage from './pages/PlayersPage.tsx'

function App() {
  return (
    <>
      <Navbar />
      <main className="page">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/matches/:id" element={<MatchPage />} />
          <Route path="/players" element={<PlayersPage />} />
          <Route path="/players/:id" element={<PlayerPage />} />
          <Route path="*" element={<StateMessage icon="🧭" title="Page not found" text="Use the menu above to find your way." />} />
        </Routes>
      </main>
    </>
  )
}

export default App
