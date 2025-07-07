import styled from '@emotion/styled';
import History from './components/History';
import Results from './components/Results';
import SelectOptions from './components/SelectOptions';
import { useResultsStore } from './stores/resultsStore';

/**
 * Styled component for the left panel
 */
const LeftPanel = styled.div`
  flex-grow: 1;
  flex-basis: 0;
  min-width: 600px;
  height: 100%;
`;

/**
 * Styled component for the right panel
 */
const RightPanel = styled.div`
  flex-grow: 0;
  flex-basis: 20%;
  min-width: 220px;
  height: 100%;
`;

/**
 * Styled component for the main layout
 */
const Layout = styled.div`
  display: flex;
  flex-direction: row;
  height: 100vh;
`;

const LeftPanelContent = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: ${({ theme }) => theme.spacing(2)};
  padding-block: 24px;
`;

function App() {
  const { results } = useResultsStore();

  return (
    <Layout>
      <LeftPanel>
        <LeftPanelContent>
          <SelectOptions />
          {results && (
            <>
              <p>Execution time: {results.execution_time} ms</p>
              <Results />
            </>
          )}
        </LeftPanelContent>
      </LeftPanel>
      <RightPanel>
        <History />
      </RightPanel>
    </Layout>
  );
}

export default App;
