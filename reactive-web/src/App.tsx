import React from 'react';
import SensorData from "./SensorData";
import MessageService from "./MessageService";

interface IProps {
}

interface IState {
    connected: boolean
    error: any
    sensorDataList: SensorData[]
}

class App extends React.Component<IProps, IState> {
    service: MessageService;

    constructor(props: IProps) {
        super(props);
        this.state = {
            connected: false,
            error: null,
            sensorDataList: []
        }
        this.service = new MessageService(
            this.handleConnection.bind(this),
            this.handleNewSensorData.bind(this)
        )
    }

    handleConnection(error: any) {
        this.setState({
            connected: !error,
            error,
        });
    }

    handleNewSensorData(error: any, sensorData: SensorData) {
        if (error) {
            console.error(error);
            this.setState({
                error,
            });
        } else {
            sensorData.instant = new Date(sensorData.instant)
            console.log(sensorData)
            this.setState({
                sensorDataList: [
                    ...this.state.sensorDataList,
                    sensorData,
                ],
            });
        }
    }

    componentDidMount() {
        this.service.connect();
    }

    componentWillUnmount() {
        this.service.close();
    }

    render() {
        return (
            <div className="container">
                <h1>reactive web app sample</h1>
                {this.state.sensorDataList.map(sensorData =>
                    <div key={sensorData.instant?.toString()}>
                        <span>{sensorData.instant?.toString()}</span> <span>{sensorData.location}</span> <span>{sensorData.temperature}</span>
                    </div>
                )}
            </div>
        );
    }
}

export default App;
