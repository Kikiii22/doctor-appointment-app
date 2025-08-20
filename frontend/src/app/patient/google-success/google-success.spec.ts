import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GoogleSuccess } from './google-success';

describe('GoogleSuccess', () => {
  let component: GoogleSuccess;
  let fixture: ComponentFixture<GoogleSuccess>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GoogleSuccess]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GoogleSuccess);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
