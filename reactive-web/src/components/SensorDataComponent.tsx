import React from 'react';
import SensorData from "../domain/SensorData";
import MessageService from "../services/MessageService";

interface IProps {
}

interface IState {
    connected: boolean
    error: any
    sensorDataMap: Map<string, SensorData[]>
}

class SensorDataComponent extends React.Component<IProps, IState> {
    service: MessageService;

    constructor(props: IProps) {
        super(props);
        this.state = {
            connected: false,
            error: null,
            sensorDataMap: new Map<string, SensorData[]>()
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

            const location = sensorData.location;
            const sensorDataMap = this.state.sensorDataMap;
            sensorDataMap.set(location, [...sensorDataMap.get(location) || [], sensorData]);

            this.setState({
                sensorDataMap: sensorDataMap
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
            <div className="row">
                {Array.from(this.state.sensorDataMap.keys()).map(location =>
                    <div className="col-sm" key={location}>
                        <h3>{location}</h3>
                        {(this.state.sensorDataMap.get(location) || []).map(sensorData => {
                            const key = location + sensorData.instant.toString()
                            return <div key={key}>
                                {sensorData.location} {sensorData.instant.toString()} {sensorData.temperature} C
                            </div>
                        })}
                    </div>
                )}
            </div>
        );
    }
}

export default SensorDataComponent;
