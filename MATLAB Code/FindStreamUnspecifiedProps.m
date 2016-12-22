function [ s_complete ] = FindStreamUnspecifiedProps( s )
%FindStreamUnspecifiedProps Streams must have at least three known properties. 
%   

%%Find Stream Unknowns
    len = size(s);
    for sNum = 1:len(1)
        %CP
        if s(sNum, 4) == 0
            s(sNum,4) = abs( s(sNum,3) / (s(sNum,2)-s(sNum,1)) );
        end
        
        %TempIn
        if s(sNum, 1) == 0
            s(sNum,1) = abs( s(sNum,2) - s(sNum,3)/s(sNum,4) );
        end
        
        %TempOut
        if s(sNum, 2) == 0
            s(sNum,2) = abs( s(sNum,1) + s(sNum,3)/s(sNum,4) );
        end
        
        %HeatLoad
        if s(sNum, 3) == 0
            s(sNum,3) = s(sNum,4)*(s(sNum,2) - s(sNum,1) );
        end
    end  

    s_complete = s;
end

