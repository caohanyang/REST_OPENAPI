import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ConfigService} from '../services/config.service';
import Config from '../models/config.model';

@Component({
  selector: 'app-step',
  templateUrl: './step.component.html',
  styleUrls: ['./step.component.scss']
})
export class StepComponent implements OnInit {

  isLinear = false;
  firstFormGroup: FormGroup;
  secondFormGroup: FormGroup;
  threeFormGroup: FormGroup;
  fourFormGroup: FormGroup;
  myValue = false;
  stuffing = 'Whitespace';
  request = 'no';
  response = 'no';
  existpara = 'no';


  constructor(
    private _formBuilder: FormBuilder,
    private configService: ConfigService
    ) { }

  //Declaring the new confige object and initilizing it
  public newConfig: Config = new Config();

  ngOnInit() {
    this.firstFormGroup = this._formBuilder.group({
      apiName: ['', Validators.required],
      docUrl: ['',  Validators.required],
      filterUrl: ['', Validators.required]
    });
    this.secondFormGroup = this._formBuilder.group({
      mode: ['', Validators.required],
      reverse: ['', Validators.required],
      stuffing: ['', Validators.required],
      urlMiddle: ['', Validators.required],
      urlAfter: ['', Validators.required],
      urlTemplate: ['', Validators.required],
      urlKey: ['', Validators.required],
      abbrevDelete: ['', Validators.required]
    });
    this.threeFormGroup = this._formBuilder.group({
      reqKey: ['', Validators.required],
      reqExample: ['', Validators.required],
      reqMiddle: ['', Validators.required],
      reqTemplate: ['', Validators.required],
      url1req2: ['', Validators.required],

      resKey: ['', Validators.required],
      resExample: ['', Validators.required],
      resMiddle: ['', Validators.required],
      resTemplate: ['', Validators.required],
      url1res2: ['', Validators.required]

    });
    this.fourFormGroup = this._formBuilder.group({
      fourCtrl: ['', Validators.required],
      test: ['', Validators.required],

      paraKey: ['', Validators.required],
      url1para2: ['', Validators.required],
      paraMiddle: ['', Validators.required],
      template: ['', Validators.required],

    });
  }

  create(): void {
    console.log(this.firstFormGroup);
    console.log(this.firstFormGroup.value.apiName);

    this.receiveForm();

    this.configService.createConfig(this.newConfig)
     .subscribe((res) => {
       console.log("-------new config------");
       console.log(this.newConfig);
       console.log("-------res.data------");
       console.log(res);
       console.log("Success to create new config");
     })
  }
  
  receiveForm(): void {
    // first form
    this.newConfig.apiName = this.firstFormGroup.value.apiName;
    this.newConfig.docUrl = this.firstFormGroup.value.docUrl;
    this.newConfig.filterUrl = this.firstFormGroup.value.filterUrl;

    this.newConfig.searchBase = false
    this.newConfig.urlBase = ""

    // second form
    this.newConfig.mode= this.secondFormGroup.value.mode;
    this.newConfig.stuffing= this.secondFormGroup.value.stuffing;
    this.newConfig.reverse= this.secondFormGroup.value.reverse;
    this.newConfig.urlKey= this.secondFormGroup.value.urlKey;
    this.newConfig.urlMiddle= this.secondFormGroup.value.urlMiddle;
    this.newConfig.urlAfter= this.secondFormGroup.value.urlAfter;
    this.newConfig.urlTemplate= this.secondFormGroup.value.urlTemplate;
    this.newConfig.abbrevDelete= this.secondFormGroup.value.abbrevDelete;
    

    this.newConfig.existVerb= "yes"
    this.newConfig.verbKey= ""
    
    // third form   request
    if (this.request === "yes" ) {
      this.newConfig.reqKey = this.threeFormGroup.value.reqKey;
    } else {
      this.newConfig.reqKey= "no"
    }
    this.newConfig.reqExample= this.threeFormGroup.value.reqExample;
    this.newConfig.reqMiddle =  this.threeFormGroup.value.reqMiddle;
    this.newConfig.url1req2 = (this.threeFormGroup.value.url1req2==="yes")? true: false;
    this.newConfig.reqTemplate= this.threeFormGroup.value.reqTemplate;

    // third form   response
    if (this.response === "yes" ) {
      this.newConfig.reqKey = this.threeFormGroup.value.resKey;
    } else {
      this.newConfig.resKey= "no"
    }
    this.newConfig.resMiddle= this.threeFormGroup.value.resMiddle;
    this.newConfig.url1res2 = (this.threeFormGroup.value.url1res2==="yes")? true: false;
    this.newConfig.resTemplate= this.threeFormGroup.value.resTemplate;
    this.newConfig.resExample= this.threeFormGroup.value.resExample;

    // four form  
    this.newConfig.existPara= (this.existpara==="yes")? true: false;
    this.newConfig.paraKey= this.fourFormGroup.value.paraKey;
    this.newConfig.url1para2= (this.fourFormGroup.value.url1para2==="yes")? true: false;
    this.newConfig.paraMiddle= this.fourFormGroup.value.paraMiddle;
    this.newConfig.template= this.fourFormGroup.value.template;

    // default
    this.newConfig.paraIn= "query"
    this.newConfig.number= "multiple"
  }


}
