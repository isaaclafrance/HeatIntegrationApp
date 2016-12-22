%%%%STREAM INFO%%%%
%%%%Property Definition: s(i,:) = [TempIn(K), TempOut(K), HeatLoad(kW), CP(kW/K)]
%%%%If property unspecified assign as 0. 
%%%%Must have at least three specified properties. 

    s = FindStreamUnspecifiedProps([ 450, 40, -1006, 0;
                                     129.9, 280, 57.4, 0;
                                     104.5, 70, -22.6, 0;
                                     249.3, 25, -26.4, 0;
                                     80, 25, -29.4, 0;
                                     35, 25, -2.2, 0;
                                     70, 35, -2, 0;
                                     183.2, 80, -336.2, 0;
                                     35.5, 450, 1016.2, 0;
                                     40, 75, 43.1, 0 ]);
    
%%%%COLUMN INFO%%%%
%%Property Definition: c(i,:) = [RebHeat(kW), RebTemp(K), CondHeat(kW), CondTemp(K)], where i<=numOfColumns.
%%All four properties must be defined.

    c = [95, 129.9, 35.7, 35.5;
         450, 40, 450, 40;
         2832, 249.3, 2552.7, 183.2]; 
     
%%%%HEAT INTEGRATION 
    disp('');
    disp('HEAT INTEGRATION WITH COLUMNS');

    %%
    deltaTMin = 10.0;
    qH_initial = 0;  
    
    [correctedQH, correctedQC, unshiftedPinchTemps, shiftedPinchTemps] = HeatIntegrator(deltaTMin, qH_initial, s, c, true, true);
 
    