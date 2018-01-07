import Config from '../models/config.model';
import { Observable} from 'rxjs/Rx';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Response} from '@angular/http';
import {Injectable} from '@angular/core';


// RxJS operator for mapping the observable
import 'rxjs/add/operator/map';

@Injectable()
export class ConfigService {

    api_url = 'http://localhost:3000';
    configUrl = `${this.api_url}/api/configs`;

    constructor(
        private http: HttpClient
    ) { }

    //Create config, takes a Config Object
    createConfig(config: Config): Observable<any> {

        return this.http.post(`${this.configUrl}`, config);
    }

    //Default Error handling method.
  private handleError(error: any): Promise<any> {
    console.error('An error occurred', error); // for demo purposes only
    return Promise.reject(error.message || error);
  }

}