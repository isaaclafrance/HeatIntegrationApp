function [minQH, minQC, unshiftedPinchTemps, shiftedPinchTemps, cascadeIntervals] = HeatIntegrator(deltaTMin, s, c, isCascadeDisplayed, isGrandCompositeCurveDisplayed)

%%Cascade Interval Matrix. (i,1)->temp1; (i,2)->temp2; (i,3)->CP; (i,4)->heat load; (i,5)->energy sent to next interval;
%%(i,6)->if values is 1 then reboiler, else if value is 2 then condenser. 
%%If reboiler then equal to 1, if condenser then equal 2, otherwise it equals 0.
%%(i,7)->column number associated with reboiler or condenser. If zero, then neither reboiler or condenser

%tic

numOfStreams = size(s,1);

%% Determine Shift Temps
    streamSTemps = s(:,1:2); %(i,1)->tempIn, (i,2)->tempOut
    columnSTemps = c(:,[2,4]); %(i,1)->reboiler temp, (i,2)->condenser temp
    %Streams
    coldStreamLogical = logical(s(:,1) < s(:,2));
    hotStreamLogical = ~coldStreamLogical;
    %(i,3)-> 1 is cold, 0 is hot
    streamSTemps = [coldStreamLogical.*( streamSTemps(:,1) + deltaTMin/2)+ hotStreamLogical.*( streamSTemps(:,1) - deltaTMin/2)...
                    ,coldStreamLogical.*( streamSTemps(:,2) + deltaTMin/2)+ hotStreamLogical.*( streamSTemps(:,2) - deltaTMin/2)];
    %Columns
    columnSTemps = [(columnSTemps(:,1) + deltaTMin/2), (columnSTemps(:,2) - deltaTMin/2)]; %reboiler treated as cold stream and condenser treated as hot stream 
    reboilerSTemps = columnSTemps(:,1);
    condenserSTemps = columnSTemps(:,2);

%% Determine Interval Shift Temperatures 
    streamSTempsVec = unique(streamSTemps(:));
    allColumnSTempsVec = columnSTemps(:);
    duplicationLogical =  ~logical(sum( repmat(streamSTempsVec', size(allColumnSTempsVec,1), 1) == repmat(allColumnSTempsVec, 1, size(streamSTempsVec, 1)) , 2));
    allColumnSTempsVec = [allColumnSTempsVec; allColumnSTempsVec(duplicationLogical)];
    streamSTempsVec = [streamSTempsVec; allColumnSTempsVec];
    intervalSTempsVec = sort(repmat(streamSTempsVec,2,1), 'descend');
    intervalSTempsVec([1,end]) = [];

%% Create Cascade Intervals
    cascadeIntervalNum = size(streamSTempsVec,1)-1;
    cascadeIntervals = zeros(cascadeIntervalNum, 7); 
    
    %Set input and output tempertaures
    intervalOutTempIndices = (1:cascadeIntervalNum)*2;
    intervalInTempIndices = intervalOutTempIndices - ones(1,cascadeIntervalNum);
    cascadeIntervals(:,[1,2]) = [intervalSTempsVec(intervalInTempIndices), intervalSTempsVec(intervalOutTempIndices)];
    %
    assignedReboilers = zeros(size(c,2),1); assignedCondensers = zeros(size(c,2),1);
    reboilerIndex = 0; condenserIndex = 0;
    for i=1:cascadeIntervalNum
        %Calculate cumulative CP for each interval that is not a column
        if(cascadeIntervals(i,1) ~= cascadeIntervals(i,2))
             indicesOfStreamsWithinTempRange = setdiff(1:numOfStreams...
                                                       ,[find(sum(streamSTemps >= cascadeIntervals(i,1), 2) == 2)...
                                                         ;find(sum(streamSTemps <= cascadeIntervals(i,2), 2) == 2)]);

            for j = indicesOfStreamsWithinTempRange
                if(coldStreamLogical(j) == 1) %Subtract cp if cold stream
                    cascadeIntervals(i,3) = cascadeIntervals(i,3) - s(j,4); %Subtract cp if cold stream
                else
                    cascadeIntervals(i,3) = cascadeIntervals(i,3) + s(j,4); %Add cp if hot stream
                end
            end        
            %Calculate heat load using cumulative CP and temperature
            %difference
            cascadeIntervals(i,4) = cascadeIntervals(i,3)* diff(cascadeIntervals(i,[2,1]));
        else  
            %Assign heat load to based on reboiler or condenser associated
            %with interval
            possibleReboilerIndices =  setdiff( [find(reboilerSTemps == cascadeIntervals(i,1));find(reboilerSTemps == cascadeIntervals(i,2))], assignedReboilers );
            possibleCondenserIndices =  setdiff( [find(condenserSTemps == cascadeIntervals(i,1)), find(condenserSTemps == cascadeIntervals(i,2))], assignedCondensers );

            if(size(possibleReboilerIndices,1) > size(possibleCondenserIndices,1))
                cascadeIntervals(i,4) = -c(possibleReboilerIndices(1), 1);
                cascadeIntervals(i,6) = 1;
                cascadeIntervals(i,7) = possibleReboilerIndices(1);
                reboilerIndex = reboilerIndex+1; 
                assignedReboilers(reboilerIndex) = possibleReboilerIndices(1); 
            else
                cascadeIntervals(i,4) = c(possibleCondenserIndices(1), 3);
                cascadeIntervals(i,6) = 2;
                cascadeIntervals(i,7) = possibleCondenserIndices(1);
                condenserIndex = condenserIndex+1; 
                assignedCondensers(condenserIndex) = possibleCondenserIndices(1); 
            end
        end
    end

%% Perform Energy Cascade.      
    %Infeasible
        cascadeIntervals(1:size(cascadeIntervals,1), 5) = cumsum(cascadeIntervals(1:size(cascadeIntervals,1), 4));
    %Feasible
        %Find heat load of interval that violates the second law of
        %thermodynamics the most. Then use that to find the corrected qH and shifted temperature pinches.
        %Then apply this correction to energy cascade.     
        pinchIntervalLoad = -min(unique(cascadeIntervals(logical(cascadeIntervals(:, 5)<0),5)));
        pinchIntervalIndices = [];
        shiftedPinchTemps = [];
        unshiftedPinchTemps = [];
        if(~isempty(pinchIntervalLoad))
            pinchIntervalIndices = find(cascadeIntervals(:, 5)== -pinchIntervalLoad);
            shiftedPinchTemps = cascadeIntervals(pinchIntervalIndices, 2);
            minQH = pinchIntervalLoad;
            cascadeIntervals(1:size(cascadeIntervals,1), 5) = cascadeIntervals(1:size(cascadeIntervals,1), 5) + ones(cascadeIntervalNum,1)*minQH; 
            
            %Find unshifted pinch temperatures based on whether the shifted
            %temperatures are associated with cold or hot streams
            streamPinchTempLogical = logical( sum(repmat(streamSTemps(:), 1, size(shiftedPinchTemps,2)) == repmat(shiftedPinchTemps', size(streamSTemps(:),1), size(streamSTemps(:),2)),2) ) ;
            columnPinchTempLogical = logical( sum(repmat(columnSTemps(:), 1, size(shiftedPinchTemps,2)) == repmat(shiftedPinchTemps', size(columnSTemps(:),1), size(columnSTemps(:),2)),2) ) ;
            s_linear = s(:, [1,2]);
            c_linear = c(:,[2,4]);     
            unshiftedPinchTemps = unique( [ s_linear(streamPinchTempLogical); c_linear(columnPinchTempLogical) ]); 
        end
        
        %Corrected qC
        minQC = cascadeIntervals(end, 5);
        
%% Display Feasible Energy Cascade 
    if (isCascadeDisplayed)
        arrowBodyLFNum = 10;
        boxTopBottomStr = '*********************';
        boxSideStr = '*                   *';

        %%Display Qh
        disp([blanks(arrowBodyLFNum), '|']);

        sTempStr = [num2str(cascadeIntervals(1, 1)), 'K '];
        spMinus = length(sTempStr);
        disp([blanks(arrowBodyLFNum-spMinus), sTempStr, '| ', 'QH: ', num2str(minQH)]);

        disp([blanks(arrowBodyLFNum), '|']);
        disp([blanks(arrowBodyLFNum-1), '\+/']);
        
        %%Display cascade
        for i = 1:size(cascadeIntervals,1)
            disp(boxTopBottomStr);
            disp(boxSideStr);

            intervalLoadStr = [num2str(cascadeIntervals(i, 4)), ' KW'];
            spMinus = length(intervalLoadStr);
            if(cascadeIntervals(i, 6) == 0)
                disp(['*',blanks(arrowBodyLFNum-ceil(spMinus/2)), intervalLoadStr]);
            elseif(cascadeIntervals(i, 6) == 1)
                disp(['*', blanks(arrowBodyLFNum-ceil(spMinus/2)), intervalLoadStr, blanks(arrowBodyLFNum-ceil(spMinus/2)) ' Reboiler of Column:', num2str(cascadeIntervals(i, 7))]);
            elseif (cascadeIntervals(i, 6) == 2)
                disp(['*', blanks(arrowBodyLFNum-ceil(spMinus/2)), intervalLoadStr, blanks(arrowBodyLFNum-ceil(spMinus/2)), ' Condenser of Column:',num2str(cascadeIntervals(i, 7))]);
            end

            disp(boxSideStr);
            disp(boxTopBottomStr);
            
            %%%%%
            disp([blanks(arrowBodyLFNum), '|']);
            
            sTempStr = [num2str(cascadeIntervals(i, 2)), 'K '];
            spMinus = length(sTempStr);
            isPinch = false;
            if logical(ismember(i, pinchIntervalIndices))
                disp([blanks(arrowBodyLFNum-spMinus), sTempStr, '| ', 'Cascade: ', num2str(cascadeIntervals(i, 5)), ' KW', '  <---- PINCH']);
                isPinch = true;
            end
            if(~isPinch)
                disp([blanks(arrowBodyLFNum-spMinus), sTempStr, '| ', 'Cascade: ', num2str(cascadeIntervals(i, 5)), ' KW']);
            end

            disp([blanks(arrowBodyLFNum), '|']);
            disp([blanks(arrowBodyLFNum-1), '\+/']);
        end 

        %%Display QC
        qCLoadStr = ['QC: ',num2str(minQC), ' KW'];
        spMinus = ceil(length(qCLoadStr)/2);
        disp([blanks(arrowBodyLFNum-spMinus), qCLoadStr]);
    end
 
 %% Display Grand Composite Curve
     if (isGrandCompositeCurveDisplayed)
        hLoadVTemp = zeros(size(cascadeIntervals,1)+1,2);
        hLoadVTemp(1,[1,2]) = [cascadeIntervals(1,1), minQH];
        hLoadVTemp(2:end,[1,2]) = cascadeIntervals(1:end, [2, 5]);

        figure;
        plot(hLoadVTemp(:,2), hLoadVTemp(:,1));

        title(['Grand Composite Curve @ deltaT = ', num2str(deltaTMin), 'K']);      
        ylabel('Shifted Temperature (K)');
        xlabel('Enthalpy (kW)');

        legend('Grand Composite Curve');
     end
    
   % toc
    
end  
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    